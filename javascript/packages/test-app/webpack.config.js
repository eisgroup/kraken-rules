const path = require('path')
const NodePolyfillPlugin = require('node-polyfill-webpack-plugin')

module.exports = env => {
    console.log(env)
    return {
        devtool: 'source-map',

        entry: ['babel-polyfill', './src/index.tsx'],

        output: {
            filename: 'app.js',
            path: path.resolve('target/dist'),
        },

        devServer: {
            port: 3001,
            open: true,
            hot: true,
            static: {
                directory: path.join(__dirname, 'target', 'dist'),
            },
        },

        resolve: {
            extensions: ['.ts', '.tsx', '.js', '.less'],
            modules: ['src', 'node_modules'],
        },

        module: {
            rules: [
                {
                    test: /\.(j|t)sx?$/,
                    exclude: /(node_modules)/,
                    use: {
                        loader: 'swc-loader',
                        options: {
                            parseMap: true,
                        },
                    },
                },
                {
                    test: /\.css$/,
                    use: ['style-loader', 'css-loader'],
                },
                {
                    test: /\.less$/,
                    use: [
                        {
                            loader: 'style-loader',
                        },
                        {
                            loader: 'css-loader',
                            options: {
                                sourceMap: true,
                            },
                        },
                        {
                            loader: 'less-loader',
                            options: {
                                sourceMap: true,
                            },
                        },
                    ],
                },
            ],
        },
        plugins: [new NodePolyfillPlugin()],
    }
}
