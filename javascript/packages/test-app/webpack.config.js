var webpack = require('webpack')
var path = require('path')

module.exports = env => {
    console.log(env)
    return {
        devtool: 'source-map',

        entry: ['babel-polyfill', './src/index.tsx'],

        output: {
            filename: 'app.js',
            publicPath: 'target/dist',
            path: path.resolve('target/dist'),
        },

        devServer: {
            port: 3001,
            historyApiFallback: true,
            inline: true,
            contentBase: path.resolve('target/dist'),
        },

        resolve: {
            extensions: ['.ts', '.tsx', '.js', '.less'],
            modules: ['src', 'node_modules'],
        },

        module: {
            loaders: [
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
        plugins: [
            new webpack.DefinePlugin({
                'process.env.NODE_ENV': JSON.stringify(env.NODE_ENV),
            }),
            new webpack.ProgressPlugin(),
        ],
    }
}
