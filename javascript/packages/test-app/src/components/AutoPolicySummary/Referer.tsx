import * as React from 'react'
import { TestProduct } from 'kraken-test-product'
import domain = TestProduct.kraken.testproduct.domain
import { Row } from 'antd'
import { renderers } from '../core/RenderInputFunctions'
import { withMetadata } from '../core/ContextHOC'
import { SingleField, InnerInputsComponentProps } from '../core/field/SingleField'
import { ContextDefinitionInfo } from '../core/field/ContextDefinitionInfo'
import { AddressInfo } from './AddressInfo'

export function Component(props: InnerInputsComponentProps<domain.Referer>): JSX.Element {
    const { id, value } = props

    function onNameChange(e: React.FormEvent<HTMLInputElement>): void {
        props.onChange(Object.assign({}, value, { name: e.currentTarget.value }))
    }

    function onAddressChange(addressInfo: domain.AddressInfo): void {
        props.onChange(Object.assign({}, value, { addressInfo }))
    }

    return (
        <React.Fragment>
            <Row>
                <SingleField
                    id={id}
                    value={value.name}
                    contextName='Referer'
                    modelFieldName='name'
                    onChange={onNameChange}
                    renderInput={renderers.input}
                />
            </Row>
            <ContextDefinitionInfo contextName='AddressInfo' />
            <AddressInfo id={value.id} onChange={onAddressChange} value={value.addressInfo} />
        </React.Fragment>
    )
}

export const Referer = withMetadata(Component)
