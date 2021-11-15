module.exports = function config(name, version) {
    return {
        src: './packages',
        out: `./target/docs/${version}`,
        name: `${name}@${version}`,
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
        theme: 'default',
        readme: 'none',
        hideGenerator: true,
        lernaExclude: ['test-app'],
    }
};
