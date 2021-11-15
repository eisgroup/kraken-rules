# Kraken bundle builder plugin

Plugin for building bundle from EntryPoint names

Required dependency `kraken-model-impl`

Required flag `-DbundlesType=SANITY` enums ALL | SANITY | COVERAGES 

Required flag `-DresourcesDir` Contexts, Rules and EntryPoint location

## Example
`mvn compile -DresourcesDir=database/gap/ -DbundlesType=SANITY`

Plugin will be activated and will take resources from `database/gap/` 
after will build all SANITY bundles as a separate files and write them into directory by bundlesType. 
