> Note, that if the time is specified without the time zone (for example `DateTime("2021-01-01T02:00:00")`), then the time will be created in the time zone of the local machine.
> It is not recommended to specify time this way, because then the actual time value depends on the time zone of the local machine on where this expression is evaluated.  
> In case of the Kraken Rule Engine, the same expression is evaluated in UI (in browser time zone) and backend (in server time zone) and therefore may result in a different rule evaluation result if time is specified without the time zone.  
> Therefore, it is recommended to specify time with explicit time zone (for example `DateTime("2021-01-01T02:00:00Z")`)
