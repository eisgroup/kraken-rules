This module is only for usage as a devDependency. 
It provides builders to create rules and rule payloads.
All expressions used in these models must be translated expressions to JavasCript environment.
Since it can be complicated and hard to maintain, suggestion is not to use expressions or use
as simple as possible.

Sample usage:
```typescript
import { RulesBuilder, PayloadBuilder } from 'kraken-model-builder'

const rule = new RulesBuilder()
    .setContext("Policy")
    .setName("Policy.state.validation")
    .setTargetPath("state")
    .setPayload(PayloadBuilder.asserts().that("state == 'CA'"))
    .build()
```