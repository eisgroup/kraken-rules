import * as React from 'react'

export const EntityBox = (props: React.PropsWithChildren<{ title: string }>) => {
    return (
        <div className='sub-entity-box'>
            <h2>{props.title}</h2>
            {props.children}
        </div>
    )
}
