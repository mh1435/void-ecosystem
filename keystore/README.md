# Signing keystore

This folder is where **you** generate and keep your personal release
keystore. Nothing here is committed to git except this file — the actual
`.jks`/`.keystore` file and `keystore.properties` are gitignored on purpose,
because leaking the keystore lets anyone sign updates that Android will
accept as coming from you.

## 1. Generate the keystore once, forever

```bash
keytool -genkeypair -v \
  -keystore keystore/void-ecosystem-release.jks \
  -alias void-ecosystem \
  -keyalg RSA -keysize 4096 -validity 10000
```

You'll be prompted for a store password and a key password — save both in a
password manager. **Never regenerate this file.** Every release you ever
ship, from every machine and from CI, must be signed with this exact
keystore. If the signing certificate changes, Android refuses to install
the new build over the old one ("App not installed" / signature mismatch)
unless you uninstall first — which is precisely the bug this setup fixes.

## 2. Point Gradle at it locally

Create `keystore.properties` in the **repo root** (gitignored):

```properties
storeFile=keystore/void-ecosystem-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=void-ecosystem
keyPassword=YOUR_KEY_PASSWORD
```

`app/build.gradle.kts` reads this file automatically when it exists and
wires it into the `release` signing config.

## 3. Give CI the same keystore

GitHub Actions can't read your local file, so the keystore is base64-encoded
and stored as a repository secret. From the repo root:

```bash
base64 -i keystore/void-ecosystem-release.jks | tr -d '\n' > keystore_base64.txt
```

Then in GitHub: **Settings → Secrets and variables → Actions → New repository
secret**, and create these four secrets:

| Secret name              | Value                                   |
|---------------------------|------------------------------------------|
| `KEYSTORE_BASE64`          | contents of `keystore_base64.txt`        |
| `KEYSTORE_STORE_PASSWORD`  | your store password                      |
| `KEYSTORE_KEY_ALIAS`       | `void-ecosystem`                         |
| `KEYSTORE_KEY_PASSWORD`    | your key password                        |

Delete `keystore_base64.txt` locally afterward — it's plaintext key material.

The workflow in `.github/workflows/build-and-release.yml` decodes this
secret back into `keystore.properties` + the `.jks` file at build time, so
every CI-built release is signed identically to your local builds.
