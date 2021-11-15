
export namespace TestProduct {

    namespace kraken.testproduct.domain {

        export interface AccessTrackInfo extends kraken.testproduct.domain.meta.Identifiable {
            createdBy?: string;
            createdOn?: Date;
            updatedBy?: string;
            updatedOn?: Date;
        }

    }

    namespace kraken.testproduct.domain {

        export interface AddressInfo extends kraken.testproduct.domain.meta.Identifiable {
            addressLine1?: kraken.testproduct.domain.AddressLine;
            addressLine2?: kraken.testproduct.domain.AddressLine;
            addressLines?: kraken.testproduct.domain.AddressLine[];
            city?: string;
            countryCd?: string;
            doNotSolicit?: boolean;
            postalCode?: string;
            street?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface AddressLine extends kraken.testproduct.domain.meta.Identifiable {
            addressLine?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface AnubisCoverage extends kraken.testproduct.domain.CarCoverage {
            cult?: kraken.testproduct.domain.Cult;
        }

    }

    namespace kraken.testproduct.domain {

        export interface BillingAddress extends kraken.testproduct.domain.AddressInfo {
        }

    }

    namespace kraken.testproduct.domain {

        export interface BillingInfo extends kraken.testproduct.domain.meta.Identifiable {
            accountName?: string;
            creditCardInfo?: kraken.testproduct.domain.CreditCardInfo;
        }

    }

    namespace kraken.testproduct.domain {

        export interface COLLCoverage extends kraken.testproduct.domain.CarCoverage {
            effectiveDate?: Date;
            expirationDate?: Date;
        }

    }

    namespace kraken.testproduct.domain {

        export interface CarCoverage extends kraken.testproduct.domain.meta.Identifiable, kraken.testproduct.domain.Coverage {
        }

    }

    namespace kraken.testproduct.domain {

        export interface Coverage {
            code?: string;
            deductibleAmount?: number;
            limitAmount?: number;
        }

    }

    namespace kraken.testproduct.domain {

        export interface CreditCardInfo extends kraken.testproduct.domain.Info {
            billingAddress?: kraken.testproduct.domain.BillingAddress;
            cardCreditLimitAmount?: { amount: number, currency: string };
            cardHolderName?: string;
            cardNumber?: string;
            cardType?: string;
            cvv?: number;
            expirationDate?: Date;
            refsToBank?: any[];
        }

    }

    namespace kraken.testproduct.domain {

        export interface Cult extends kraken.testproduct.domain.meta.Identifiable {
            date?: Date;
            name?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface DriverInfo extends kraken.testproduct.domain.meta.Identifiable {
            convicted?: boolean;
            driverType?: string;
            trainingCompletionDate?: Date;
        }

    }

    namespace kraken.testproduct.domain {

        export interface FullCoverage extends kraken.testproduct.domain.meta.Identifiable, kraken.testproduct.domain.Coverage {
            effectiveDate?: Date;
            expirationDate?: Date;
            typeOfInjuryCovered?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface Info extends kraken.testproduct.domain.meta.Identifiable {
            additionalInfo?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface Insured extends kraken.testproduct.domain.meta.Identifiable {
            addressInfo?: kraken.testproduct.domain.BillingAddress;
            childrenAges?: number[];
            haveChildren?: boolean;
            name?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface Party extends kraken.testproduct.domain.meta.Identifiable {
            driverInfo?: kraken.testproduct.domain.DriverInfo;
            personInfo?: kraken.testproduct.domain.PersonInfo;
            relationToPrimaryInsured?: string;
            roles?: kraken.testproduct.domain.PartyRole[];
        }

    }

    namespace kraken.testproduct.domain {

        export interface PartyRole extends kraken.testproduct.domain.meta.Identifiable {
            limit?: number;
            role?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface PersonInfo extends kraken.testproduct.domain.Info {
            addressInfo?: kraken.testproduct.domain.AddressInfo;
            age?: number;
            firstName?: string;
            lastName?: string;
            occupation?: string;
            sameHomeAddress?: boolean;
        }

    }

    namespace kraken.testproduct.domain {

        export interface Policy extends kraken.testproduct.domain.meta.Identifiable {
            accessTrackInfo?: kraken.testproduct.domain.AccessTrackInfo;
            billingInfo?: kraken.testproduct.domain.BillingInfo;
            coverage?: kraken.testproduct.domain.CarCoverage;
            createdFromPolicyRev?: number;
            insured?: kraken.testproduct.domain.Insured;
            multiInsureds1?: kraken.testproduct.domain.SecondaryInsured[];
            multiInsureds2?: kraken.testproduct.domain.SecondaryInsured[];
            multipleInsureds?: kraken.testproduct.domain.SecondaryInsured[];
            oneInsured?: kraken.testproduct.domain.SecondaryInsured;
            parties?: kraken.testproduct.domain.Party[];
            policies?: string[];
            policyCurrency?: string;
            policyDetail?: kraken.testproduct.domain.PolicyDetail;
            policyNumber?: string;
            refToCustomer?: any;
            referer?: kraken.testproduct.domain.Referer;
            riskItems?: kraken.testproduct.domain.Vehicle[];
            state?: string;
            termDetails?: kraken.testproduct.domain.TermDetails;
            transactionDetails?: kraken.testproduct.domain.TransactionDetails;
        }

    }

    namespace kraken.testproduct.domain {

        export interface PolicyDetail extends kraken.testproduct.domain.meta.Identifiable {
            currentQuoteInd?: boolean;
            oosProcessingStage?: string;
            versionDescription?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface RRCoverage extends kraken.testproduct.domain.RentalCoverage {
            combinedLimit?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface Referer extends kraken.testproduct.domain.meta.Identifiable {
            addressInfo?: kraken.testproduct.domain.AddressInfo;
            name?: string;
            refererInfo?: kraken.testproduct.domain.RefererInfo;
            superReferer?: kraken.testproduct.domain.SuperReferer;
        }

    }

    namespace kraken.testproduct.domain {

        export interface RefererInfo extends kraken.testproduct.domain.meta.Identifiable {
            referenceName?: string;
            referer?: kraken.testproduct.domain.Referer;
            refererInfo?: kraken.testproduct.domain.RefererInfo;
        }

    }

    namespace kraken.testproduct.domain {

        export interface RentalCoverage extends kraken.testproduct.domain.meta.Identifiable, kraken.testproduct.domain.Coverage {
        }

    }

    namespace kraken.testproduct.domain {

        export interface SecondaryInsured extends kraken.testproduct.domain.meta.Identifiable {
            name?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface SuperReferer extends kraken.testproduct.domain.Referer {
        }

    }

    namespace kraken.testproduct.domain {

        export interface TermDetails extends kraken.testproduct.domain.meta.Identifiable {
            contractTermTypeCd?: string;
            termCd?: string;
            termEffectiveDate?: Date;
            termExpirationDate?: Date;
            termNo?: number;
        }

    }

    namespace kraken.testproduct.domain {

        export interface TransactionDetails extends kraken.testproduct.domain.meta.Identifiable {
            changePremium?: number;
            totalLimit?: number;
            totalPremium?: number;
            txCreateDate?: Date;
            txEffectiveDate?: Date;
            txReason?: string;
            txType?: string;
        }

    }

    namespace kraken.testproduct.domain {

        export interface Vehicle extends kraken.testproduct.domain.meta.Identifiable {
            addressInfo?: kraken.testproduct.domain.AddressInfo;
            anubisCoverages?: kraken.testproduct.domain.AnubisCoverage[];
            collCoverages?: kraken.testproduct.domain.COLLCoverage[];
            costNew?: number;
            declaredAnnualMiles?: number;
            fullCoverages?: kraken.testproduct.domain.FullCoverage[];
            included?: boolean;
            model?: string;
            modelYear?: number;
            newValue?: number;
            numDaysDrivenPerWeek?: number;
            odometerReading?: number;
            purchasedDate?: Date;
            rentalCoverage?: kraken.testproduct.domain.RRCoverage;
            serviceHistory?: Date[];
            vehicleState?: string;
        }

    }

    namespace kraken.testproduct.domain.meta {

        export interface Identifiable {
            cd?: string;
            id?: string;
        }

    }

}
