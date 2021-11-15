/**
 * Creates config for typedoc plugin
 * Package name, version and folder will be resolved.
 * 
 * @param {string} packageFolderName    name of folder in directory 'packages'
 *                                      example: 'model'
 */
function createConfig(packageFolderName) {
    var path = './packages/' + packageFolderName;
    var package = require(`../.${path}/package.json`);
    var version = package.version;

    return {
        src: path,
        out: `./target/docs/${version}/${package.name}/`,
        name: `${package.name}@${version}`,
        readme: `${path}/docs.md`,
        mode: "modules",

        ignoreCompilerErrors: true,
        excludeExternals: true,
        excludePrivate: true,
        externalPattern: "**/node_modules/**/*",
        exclude: [
            "__tests__",
            "node_modules",
            "test/",
            "node_modules/",
            "**/test",
            "**/node_modules",
            "**/__tests__/**/*",
            "**/node_modules/**/*",
        ],
        theme: 'minimal',
        hideGenerator: true
    }
}

module.exports = createConfig;