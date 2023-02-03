# Kraken TypeScript engine

- [Kraken TypeScript engine](#kraken-typescript-engine)
  - [Start](#start)
    - [Update/install dependencies only](#updateinstall-dependencies-only)
  - [Publish](#publish)
    - [Release](#release)
    - [Stage](#stage)
  - [Add license headers](#add-license-headers)
  - [Generate documentation](#generate-documentation)
  - [Test Application](#test-application)
    - [Development app](#development-app)
    - [Production app](#production-app)
  - [Formatting](#formatting)
    - [Ignore](#ignore)
    - [IDE integration](#ide-integration)
    - [pre commit hook](#pre-commit-hook)
## Start

### Yarn

If not installed, install yarn globally:

```shell
npm i yarn -g
```

To install interdependencies and link inner dependencies:

```shell
yarn
```

To compile, transpile, lint packages:

```shell
yarn bootstrap
```

## Publish 

### Release
```bash
yarn
yarn release # with no parameters it will automatically create staging version
# was 1.1.1
# now 1.1.1-stage.0

# was 1.1.1-stage.0
# now 1.1.1-stage.1
```
```bash
yarn
yarn release -- -v 1.1.1 # set explicit version. Use only Semantic versioning convention
```
### Stage 
This script is used in CI. It changes version, but do not commits and adds tag.
```bash
yarn
yarn bootstrap
yarn stage -- -v 1.1.1 # set explicit version. Use only Semantic versioning convention
```
## Add license headers 

To add license headers run this command.

```bash
yarn licence-headers
```

## Generate documentation

To generate static web application from TypeScript `jsdoc`s run this command
```bash
yarn
yarn docs
```
This command will generate static web application fol all packages defined 
in `typedoc.json` in folder `./target/docs/<version>`.

## Test Application

To start test application need to build project (find [start section](#start) above).
### Development app
Development application will always refer to the `localhost:8888`
```
cd packages/test-app
yarn start
```

### Production app
Application will try to resolve backend uri from `<host>:<port>/path`. If this file is absent it will refer to the `localhost:8888`
```
cd packages/test-app
yarn serve
```

Backend resolution algorithm is:
```bash
if "/path" exists
  then resolve from "/path"
  else "localhost:8888" 
```
## Formatting
### Ignore

Ignore file formatting with `// prettier-ignore` ([docs](https://prettier.io/docs/en/ignore.html#range-ignore))

### IDE integration

-   [vscode](https://prettier.io/docs/en/editors.html#visual-studio-code) add vscode plugin `prettier-vscode` and add
    this configuration to `.vscode\settings.json`

```json
"editor.formatOnSave": false,
"editor.defaultFormatter": "esbenp.prettier-vscode",
"[typescript]": {
  "editor.defaultFormatter": "esbenp.prettier-vscode"
},
"json.format.enable": false,
"prettier.configPath": ".prettierrc.json"
```

Now vscode will automatically format your typescript files on save with prettier.

-   [intelliJ IDEA](https://prettier.io/docs/en/webstorm.html)

### pre commit hook

for a precommit hook add this file to `<repo-root>/.git/hooks/pre-commit`

```sh
#!/bin/sh
FILES=$(git diff --cached --name-only --diff-filter=ACMR javascript/ | sed 's| |\\ |g')
[ -z "$FILES" ] && exit 0

# Prettify all selected files
echo "$FILES" | xargs ./javascript/node_modules/.bin/prettier --ignore-unknown --write --config ./javascript/.prettierrc.json --ignore-path  ./javascript/.prettierignore

# Add back the modified/prettified files to staging
echo "$FILES" | xargs git add

exit 0
```
