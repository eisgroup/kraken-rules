# Kraken demo application

## Application usage
In this application synchronous kraken ui engine is used. 
On model `state` field is dimensions field. Value from `state` input will be passed as dimension.

```javascript
// evaluation context
{
    dimensions: {
        state: 'CA'
    }
}
```

To load new rules by dimensions use button `Fetch new rules`at the top toolbar

## Building application

To bundle application:
```bash
yarn run pack
```

To start application on server: (by default will start on `localhost:5000`)
```bash
yarn run serve
```
```bash
yarn run serve -- -p <PORT>
```

To start application for development (run index.html file in target/dist folder)
```bash
yarn start
```
```bash
yarn start -- --port <PORT>
```

