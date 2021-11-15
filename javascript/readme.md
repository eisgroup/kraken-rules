# Kraken TypeScript engine

- [Kraken TypeScript engine](#kraken-typescript-engine)
  - [Start](#start)
  - [Publish](#publish)
    - [Release](#release)
    - [Stage](#stage)
  - [Add license headers](#add-license-headers)
  - [Generate documentation](#generate-documentation)
  - [Test Application](#test-application)
    - [Development app](#development-app)
    - [Production app](#production-app)
## Start

To start working with project run in command line.

```bash
npm i
npx lerna bootstrap
```

It will install dependencies, link inner dependencies, build all projects.
[(lerna bootstrap docs)](https://github.com/lerna/lerna/tree/master/commands/bootstrap#readme)


## Publish 

### Release
```bash
npm i
npm run release # with no parameters it will automatically create staging version
# was 1.1.1
# now 1.1.1-stage.0

# was 1.1.1-stage.0
# now 1.1.1-stage.1
```
```bash
npm i
npm run release -- -v 1.1.1 # set explicit version. Use only Semantic versioning convention
```
### Stage 
This script is used in CI. It changes version, but do not commits and adds tag.
```bash
npm i
npx lerna bootstrap
npm run stage -- -v 1.1.1 # set explicit version. Use only Semantic versioning convention
```
## Add license headers 

To add license headers run this command.

```bash
npm run licence-headers
```

## Generate documentation

To generate static web application from TypeScript `jsdoc`s run this command
```bash
npm i
npm run docs
```
This command will generate static web application in folder `./target/docs/<version>`.
Packages marked in `package.json` `private:true` will be ignored.

## Test Application

To start test application need to build project (find [start section](#start) above).
### Development app
Development application will always refer to the `localhost:8888`
```
cd packages/test-app
npm start
```

### Production app
Application will try to resolve backend uri from `<host>:<port>/path`. If this file is absent it will refer to the `localhost:8888`
```
cd packages/test-app
npm run serve
```

Backend resolution algorithm is:
```bash
if "/path" exists
  then resolve from "/path"
  else "localhost:8888" 
```