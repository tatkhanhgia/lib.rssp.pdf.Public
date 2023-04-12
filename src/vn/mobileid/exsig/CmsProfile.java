/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.mobileid.exsig;

import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.PdfPKCS7V4;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.esf.RevocationValues;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.CertificateList;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateStatus;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataParser;
import org.bouncycastle.cms.CMSTypedStream;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Selector;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.StoreException;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.mobileid.util.Utils;

/**
 *
 * @author minhg
 */
public class CmsProfile extends Profile {

    private transient final Logger log = LoggerFactory.getLogger(CmsProfile.class);

    public CmsProfile(PKCS7Form form, Algorithm algorithm) {
        super(form, algorithm);
        this.form = form;
    }

    @Override
    List<byte[]> appendSignautre(List<String> signatureList) throws Exception {
        Calendar signingTime = Calendar.getInstance();
        signingTime.setTimeInMillis(timeMillis);
        TSAClient tsaClient = null;
        if (form.isTsa()) {
            tsaClient = new TSAClientBouncyCastle(tsaData[0], tsaData[1], tsaData[2], 8192, algorithm.getValue());
        }
        X509Certificate[] cert = this.certificates.toArray(new X509Certificate[certificates.size()]);
        List<byte[]> result = new ArrayList<>();

        for (int i = 0; i < otherList.size(); i++) {
            BouncyCastleDigest digest = new BouncyCastleDigest();
            PdfPKCS7V4 sgn = new PdfPKCS7V4(null, cert, algorithm.getValue(), null, digest, false);

            byte[] extSignature = Base64.decode(signatureList.get(i));
            sgn.setExternalDigest(extSignature, null, "RSA");
            byte[] signedContent = sgn.getEncodedPKCS7(
                    otherList.get(i),
                    signingTime,
                    tsaClient,
                    ocsp,
                    crls,
                    MakeSignature.CryptoStandard.CADES);
            result.add(signedContent);
        }
        return result;
    }

    @Override
    public void generateHash(List<byte[]> dataToBeSign) throws Exception {

        if (form.isRevocation()) {
            if (certificates.size() >= 2 && crls.isEmpty()) {
                OcspClient ocspClient = new OcspClientBouncyCastle();
                ocsp = ocspClient.getEncoded(certificates.get(0), certificates.get(1), null);
                if (ocsp != null) {
                    ltvSize = ltvSize + ocsp.length;
                }
            }
        }

        Calendar signingTime = Calendar.getInstance();
        signingTime.setTimeInMillis(timeMillis);
        X509Certificate[] cert = this.certificates.toArray(new X509Certificate[certificates.size()]);
        for (int i = 0; i < dataToBeSign.size(); i++) {
            BouncyCastleDigest digest = new BouncyCastleDigest();
            PdfPKCS7V4 sgn = new PdfPKCS7V4(null, cert, algorithm.getValue(), null, digest, false);
            byte[] hash = DigestAlgorithms.digest(
                    new ByteArrayInputStream(dataToBeSign.get(i)), digest.getMessageDigest(algorithm.getValue())
            );
            otherList.add(hash);
            byte[] sh = sgn.getAuthenticatedAttributeBytes(
                    hash,
                    signingTime,
                    ocsp,
                    crls,
                    MakeSignature.CryptoStandard.CADES);
            byte[] hashData = DigestAlgorithms.digest(new ByteArrayInputStream(sh), digest.getMessageDigest(algorithm.getValue()));
            hashList.add(new String(Base64.encode(hashData)));
        }
    }

    public static List<VerifyResult> verify(byte[] originalData, byte[] signedData, boolean certificateStatusEnabled) throws Exception {
        CmsVerify cmsVerify = new CmsVerify();
        return cmsVerify.verifySignature(originalData, signedData, certificateStatusEnabled);
    }

}

class CmsVerify {

    public List<VerifyResult> verifySignature(
            byte[] originalData,
            byte[] signedData,
            boolean revocationEnabled) throws IOException {
        List<VerifyResult> verifyResults = null;
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            verifyResults = new ArrayList<>();
            InputStream is = new ByteArrayInputStream(originalData);
            CMSSignedDataParser sp = new CMSSignedDataParser(
                    new BcDigestCalculatorProvider(),
                    new CMSTypedStream(is),
                    signedData);
            CMSTypedStream signedContent = sp.getSignedContent();
            signedContent.drain();
            Store certStore = sp.getCertificates();
            SignerInformationStore signers = sp.getSignerInfos();
            Collection c = signers.getSigners();
            Iterator it = c.iterator();

            while (it.hasNext()) {
                SignerInformation signer = (SignerInformation) it.next();
                Collection certCollection = certStore.getMatches(signer.getSID());
                Iterator certIt = certCollection.iterator();
                X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
                Collection x509CertCollection = certStore.getMatches(new Selector() {
                    @Override
                    public Object clone() {
                        return new Object();
                    }

                    @Override
                    public boolean match(Object t) {
                        return true;//To change body of generated methods, choose Tools | Templates.
                    }
                });
                List<X509CertificateHolder> holders = new ArrayList<>();
                holders.addAll(x509CertCollection);
                x509CertCollection.addAll(x509CertCollection);
                List<X509Certificate> x509CertList = new ArrayList<>();
                JcaX509CertificateConverter x509CertificateConverter = new JcaX509CertificateConverter()
                        .setProvider(new BouncyCastleProvider());
                for (X509CertificateHolder holder : holders) {
                    x509CertList.add(x509CertificateConverter.getCertificate(holder));
                }
                String date = null;
                String form = null;
                String id = null;
                String signingMethod = null;
                X509Certificate x509 = null;
                boolean valid = false;
                Date dateTime = null;

                try {
                    if (!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().build(certHolder))) {
                        id = signer.getSID().getSerialNumber().toString();
                        throw new Exception("Invalid sigature");
                    } else {
                        valid = true;
                        form = "CMS";
                        MessageDigest md = MessageDigest.getInstance(
                                signer.getDigestAlgOID(),
                                BouncyCastleProvider.PROVIDER_NAME);
                        signingMethod = md.getAlgorithm();
                        x509 = x509CertificateConverter.getCertificate(certHolder);
                        AttributeTable atab = signer.getSignedAttributes();
                        id = signer.getSID().getSerialNumber().toString();
                        if (atab != null) {

                            Attribute attr = atab.get(CMSAttributes.signingTime);
                            if (attr != null) {
                                Time t = Time.getInstance(attr.getAttrValues().getObjectAt(0)
                                        .toASN1Primitive());
                                dateTime = t.getDate();
                            }

                            AttributeTable unAtab = signer.getUnsignedAttributes();
                            if (unAtab != null) {
                                Attribute ucv1 = atab.get(PKCSObjectIdentifiers.id_aa_signingCertificate);
                                if (ucv1 != null) {
                                    form = "CAdES-B";
                                }

                                Attribute ucv2 = atab.get(PKCSObjectIdentifiers.id_aa_signingCertificateV2);
                                if (ucv2 != null) {
                                    form = "CAdES-B";
                                }
                                Attribute unAttr = unAtab.get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
                                if (attr != null) {
                                    if (unAttr.getAttrValues() instanceof DERSet) {
                                        DERSet tsSet = (DERSet) unAttr.getAttrValues();
                                        DERSequence tsSeq = (DERSequence) tsSet.getObjectAt(0);
                                        TimeStampToken tsToken = new TimeStampToken(
                                                new CMSSignedData(tsSeq.getEncoded("DER")));
                                        dateTime = tsToken.getTimeStampInfo().getGenTime();
                                        Store storeTt = tsToken.getCertificates();
                                        Collection collTt = storeTt.getMatches(tsToken.getSID());
                                        Iterator certIt2 = collTt.iterator();
                                        X509CertificateHolder cert2 = (X509CertificateHolder) certIt2.next();
                                        tsToken.validate(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert2));
                                        form = "CAdES-T";
                                    }
                                }
                            }

                            if (form.equals("CAdES-T")) {
                                Attribute revAttr = atab.get(new ASN1ObjectIdentifier("1.2.840.113583.1.1.8"));
                                if (revAttr != null) {
                                    try {
                                        if (x509CertList.size() == 1) {
                                            x509CertList = Utils.getCertPath(x509);
                                        }
                                        List<X509CRL> crlList = new ArrayList<>();
                                        RevocationValues revValues = RevocationValues.getInstance(revAttr.getAttrValues().getObjectAt(0));
                                        if (revValues.getCrlVals() != null) {
                                            for (CertificateList revValue : revValues.getCrlVals()) {
                                                X509CRLHolder x509CRLHolder = new X509CRLHolder(revValue);
                                                crlList.add(new JcaX509CRLConverter().setProvider("BC").getCRL(x509CRLHolder));
                                            }
                                            List<X509Certificate> certPath = Utils.getCertPath(x509, true, dateTime, x509CertList, crlList);
                                            if (certPath != null) {
                                                form = "CAdES-LT";
                                            }
                                        }
                                        if (revValues.getOcspVals() != null) {
                                            for (BasicOCSPResponse basicOCSPResponse : revValues.getOcspVals()) {
                                                BasicOCSPResp basicOCSPResp = new BasicOCSPResp(basicOCSPResponse);
                                                if (basicOCSPResp.getResponses()[0].getCertStatus().equals(CertificateStatus.GOOD)) {
                                                    form = "CAdES-LT";
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        throw new Exception("Invalid revocation");
                                    }
                                }
                            }
                        }

                        if (revocationEnabled && !form.equals(PKCS7Form.LT.toString())) {
                            valid = false;
                            if (Utils.getCertificatePath(x509) != null) {
                                valid = true;
                            }
                        }
                    }
                } catch (Exception ex) {

                }

                if (dateTime != null) {
                    date = Utils.timeMilsToString(dateTime.getTime());
                }
                verifyResults.add(new VerifyResult(id, form, x509, signingMethod, date, valid));
            }

        } catch (IOException | CMSException | StoreException | CertificateException ex) {
            throw new IOException("Can't load document", ex);
        }
        return verifyResults;
    }

}
