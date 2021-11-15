package kraken.benchmarks.engine;

import kraken.testproduct.domain.*;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
public class PolicyBuilder {
    
    private static <T> List<T> list(Supplier<T> supplier, int i) {
        ArrayList<T> objects = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            objects.add(supplier.get());
        }
        return objects;
    }

    public static Policy getPolicy(int multiplier) {
        Policy policy = new Policy();
        policy.setInsured(getInsured());
        policy.setBillingInfo(getBillingInfo());
        policy.setParties(list(PolicyBuilder::getParty, multiplier));
        policy.setRiskItems(list(PolicyBuilder::getRiskItem, multiplier));
        policy.setAccessTrackInfo(getAccessTrackInfo());
        policy.setCreatedFromPolicyRev(1);
        policy.setMultipleInsureds(
                getSecondaryInsured(),
                list(PolicyBuilder::getSecondaryInsured, multiplier),
                list(PolicyBuilder::getSecondaryInsured, multiplier),
                list(PolicyBuilder::getSecondaryInsured, multiplier)
        );
        policy.setPolicies(List.of("1", "12", "123"));
        policy.setPolicyCurrency("USD");
        policy.setPolicyDetail(getPolicyDetail());
        policy.setTermDetails(getTermDetails());
        policy.setRefToCustomer("a");
        policy.setState("ca");
        policy.setPolicyCurrency("USD");
        policy.setTransactionDetails(getTransactionDetails());
        return policy;
    }

    public static TransactionDetails getTransactionDetails() {
        TransactionDetails d = new TransactionDetails();
        d.setId(id());
        d.setChangePremium(new BigDecimal(123));
        d.setTotalPremium(new BigDecimal(123));
        d.setTxReason("reason");
        d.setTxType("type");
        d.setTxEffectiveDate(LocalDateTime.MIN);
        d.setTxCreateDate(LocalDate.MAX);
        return d;
    }

    public static TermDetails getTermDetails() {
        TermDetails termDetails = new TermDetails();
        termDetails.setContractTermTypeCd("cd");
        termDetails.setTermCd("term");
        termDetails.setTermEffectiveDate(LocalDate.MIN);
        termDetails.setTermExpirationDate(LocalDate.MAX);
        termDetails.setId(id());
        return termDetails;
    }

    public static PolicyDetail getPolicyDetail() {
        PolicyDetail policyDetail = new PolicyDetail();
        policyDetail.setCurrentQuoteInd(true);
        policyDetail.setOosProcessingStage("oos");
        policyDetail.setVersionDescription("desc");
        policyDetail.setId(id());
        return policyDetail;
    }

    public static SecondaryInsured getSecondaryInsured() {
        return new SecondaryInsured("name", id());
    }

    public static AccessTrackInfo getAccessTrackInfo() {
        AccessTrackInfo accessTrackInfo = new AccessTrackInfo();
        accessTrackInfo.setCreatedBy("qwe");
        accessTrackInfo.setCreatedOn(LocalDate.MAX);
        accessTrackInfo.setUpdatedBy("qwe");
        accessTrackInfo.setUpdatedOn(LocalDate.MAX);
        accessTrackInfo.setId(id());
        return accessTrackInfo;
    }

    public static Vehicle getRiskItem() {
        Vehicle vehicle = new Vehicle();
        vehicle.setAddressInfo(getAddressInfo());
        vehicle.setId(id());
        vehicle.setModel("volvo");
        vehicle.setModelYear(1999);
        vehicle.setCostNew(new BigDecimal(120000));
        vehicle.setDeclaredAnnualMiles(1000l);
        vehicle.setIncluded(false);
        vehicle.setNumDaysDrivenPerWeek(3);
        vehicle.setServiceHistory(List.of(
                LocalDate.ofYearDay(2011, 1),
                LocalDate.ofYearDay(2009, 1),
                LocalDate.ofYearDay(2007, 1)
                )
        );
        vehicle.setPurchasedDate(LocalDate.MIN);
        vehicle.setNumDaysDrivenPerWeek(5);
        vehicle.setAnubisCoverages(list(PolicyBuilder::getAnubis, 10));
        vehicle.setCollCoverages(list(PolicyBuilder::getCollCoverage, 10));
        vehicle.setFullCoverages(list(PolicyBuilder::getFullCoverage, 10));
        vehicle.setRentalCoverage(getRentalCoverage());
        return vehicle;
    }

    private static RRCoverage getRentalCoverage() {
        RRCoverage rrCoverage = new RRCoverage();
        rrCoverage.setId(id());
        rrCoverage.setCombinedLimit("dasda");
        rrCoverage.setCode("code");
        rrCoverage.setDeductibleAmount(new BigDecimal(123));
        rrCoverage.setLimitAmount(new BigDecimal(123));
        return rrCoverage;
    }

    public static FullCoverage getFullCoverage() {
        FullCoverage coverage = new FullCoverage();
        coverage.setEffectiveDate(LocalDate.MIN);
        coverage.setExpirationDate(LocalDate.MAX);
        coverage.setId(id());
        coverage.setDeductibleAmount(new BigDecimal(123));
        coverage.setLimitAmount(new BigDecimal(123));
        coverage.setCode("code");
        coverage.setTypeOfInjuryCovered("spinal");
        return coverage;
    }

    public static COLLCoverage getCollCoverage() {
        COLLCoverage collCoverage = new COLLCoverage();
        collCoverage.fill();
        collCoverage.setEffectiveDate(LocalDate.MIN);
        collCoverage.setExpirationDate(LocalDate.MAX);
        collCoverage.setId(id());
        return collCoverage;
    }

    public static AnubisCoverage getAnubis() {
        AnubisCoverage anubisCoverage = new AnubisCoverage("cd", new BigDecimal(11), new BigDecimal(112), "sun", LocalDate.MAX);
        anubisCoverage.setId(id());
        return anubisCoverage;
    }

    public static Party getParty() {
        Party party = new Party();
        party.setPersonInfo(getPersonInfo());
        party.setDriverInfo(getDriverInfo());
        party.setRelationToPrimaryInsured("parent");
        party.setRoles(list(PolicyBuilder::getRole, 10));
        party.setId(id());
        return party;
    }

    public static PartyRole getRole() {
        PartyRole partyRole = new PartyRole();
        partyRole.setRole("roleA");
        partyRole.setId(id());
        return partyRole;
    }

    public static DriverInfo getDriverInfo() {
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setConvicted(false);
        driverInfo.setDriverType("f1");
        driverInfo.setTrainingCompletionDate(LocalDate.MAX);
        driverInfo.setId(id());
        return driverInfo;
    }

    public static PersonInfo getPersonInfo() {
        PersonInfo personInfo = new PersonInfo();
        personInfo.setAddressInfo(getAddressInfo());
        personInfo.setAge(11);
        personInfo.setFirstName("jonas");
        personInfo.setLastName("braziulis");
        personInfo.setOccupation("occupied");
        personInfo.setSameHomeAddress(false);
        personInfo.setAdditionalInfo("aaa");
        personInfo.setId(id());
        return personInfo;
    }

    public static BillingInfo getBillingInfo() {
        BillingInfo billingInfo = new BillingInfo();
        billingInfo.setAccountName("name");
        billingInfo.setCreditCardInfo(getCreditCardInfo());
        billingInfo.setId(id());
        return billingInfo;
    }

    public static CreditCardInfo getCreditCardInfo() {
        return new CreditCardInfo("visa", "12341232412343", 111, getBillingAddressInfo(), getMoney());
    }

    public static MonetaryAmount getMoney() {
        return Monetary.getDefaultAmountFactory().setNumber(new Random().nextInt()).setCurrency("USD").create();
    }

    public static Insured getInsured() {
        Insured insured = new Insured();
        insured.setChildrenAges(List.of(1, 1, 2, 3));
        insured.setHaveChildren(false);
        insured.setName("jonas");
        insured.setId(id());
        insured.setAddressInfo(getBillingAddressInfo());
        return insured;
    }

    public static BillingAddress getBillingAddressInfo() {
        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setAddressLine1(new AddressLine1("qwe"));
        billingAddress.setAddressLine2(new AddressLine2("asd"));
        billingAddress.setCd("BillingAddress");
        billingAddress.setCity("vilnius");
        billingAddress.setCountryCd("lt");
        billingAddress.setPostalCode("AZ12321232");
        billingAddress.setStreet("ulonu");
        billingAddress.setDoNotSolicit(false);
        billingAddress.setId(id());
        return billingAddress;
    }

    public static AddressInfo getAddressInfo() {
        AddressInfo addressInfo = new AddressInfo();
        addressInfo.setAddressLine1(new AddressLine1("qwe"));
        addressInfo.setAddressLine2(new AddressLine2("asd"));
        addressInfo.setCd("BillingAddress");
        addressInfo.setCity("vilnius");
        addressInfo.setCountryCd("lt");
        addressInfo.setPostalCode("AZ12321232");
        addressInfo.setStreet("ulonu");
        addressInfo.setDoNotSolicit(false);
        addressInfo.setId(id());
        return addressInfo;
    }

    public static String id() {
        return UUID.randomUUID().toString();
    }

}
