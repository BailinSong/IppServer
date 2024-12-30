package ooo.reindeer.ipp.server;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.PrinterState;
import com.hp.jipp.model.PrinterStateReason;
import kotlin.ranges.IntRange;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static com.hp.jipp.model.Operation.*;
import static com.hp.jipp.model.SystemConfiguredPrinters.printerInfo;
import static com.hp.jipp.model.Types.*;
import static org.slf4j.LoggerFactory.getLogger;

public class IppPrinter {
    final static Logger logger = getLogger(IppPrinter.class);

    private final Date startTime;





    private final String name;
    private final String uuid;
    int version = 0x200;
    String[] supportedVersion = {"2.0"};
    String charset = "utf-8";
    String locale = "en-us";
    String language = "en";
    String defaultFormat = "application/pdf";
    String[] supportedFormats = {"application/pdf", "application/octet-stream", "text/plain"};
    PrinterState state = PrinterState.idle;
    String stateReason = PrinterStateReason.none;
    Map<String, Attribute<?>> printerAttributes = new ConcurrentHashMap<String, Attribute<?>>();


    Attribute<?> defaultEmptyAttribute = null;

    IIppPrinterEventHandler handler;

    public void setHandler(IIppPrinterEventHandler handler) {
        this.handler = handler;
    }

    public IIppPrinterEventHandler getHandler() {
        return handler;
    }

    public Attribute<?> getDefaultEmptyAttribute() {
        return defaultEmptyAttribute;
    }

    public void setDefaultEmptyAttribute(Attribute<?> defaultEmptyAttribute) {
        this.defaultEmptyAttribute = defaultEmptyAttribute;
    }

    private Attribute<?>[] OPERATION_ATTRIBUTES = {
            attributesCharset.of("utf-8"),
            attributesNaturalLanguage.of("en-us")
    };

    public Attribute<?> putAttribute(Attribute<?> attribute) {
        Attribute<?> oldAttribute = printerAttributes.put(attribute.getName(), attribute);
        handler.onPrintAttributeSet(this, oldAttribute, attribute);
        return oldAttribute;
    }

    public Attribute<?> getAttribute(String name) {
        return printerAttributes.getOrDefault(name, defaultEmptyAttribute);
    }


    public IppPrinter(String name, String uuid,IIppPrinterEventHandler handler) throws URISyntaxException {
        this.name = name;
        this.uuid = uuid;
        this.startTime = new Date();
        this.handler = handler;


        putAttribute(printerName.of(name));
        putAttribute(printerUuid.of(new URI(uuid)));
        putAttribute(printerState.of(state));
        putAttribute(printerStateReasons.of(stateReason));
        putAttribute(ippVersionsSupported.of(Arrays.asList(supportedVersion)));
        putAttribute(operationsSupported.of(getPrinterAttributes, getJobAttributes, validateJob, createJob, printJob));
//        putAttribute(operationsSupported.of(printJob,
//                printUri,
//                validateJob,
//                createJob,
//                sendDocument,
//                sendUri,
//                cancelJob,
//                getJobAttributes,
//                getJobs,
//                getPrinterAttributes,
//                holdJob,
//                releaseJob,
//                restartJob,
//                pausePrinter,
//                resumePrinter,
//                purgeJobs,
//                setPrinterAttributes,
//                setJobAttributes,
//                getPrinterSupportedValues,
//                createPrinterSubscriptions,
//                createJobSubscriptions,
//                getSubscriptionAttributes,
//                getSubscriptions,
//                renewSubscription,
//                cancelSubscription,
//                getNotifications,
//                getResourceAttributes,
//                getResources,
//                enablePrinter,
//                disablePrinter,
//                pausePrinterAfterCurrentJob,
//                holdNewJobs,
//                releaseHeldNewJobs,
//                deactivatePrinter,
//                activatePrinter,
//                restartPrinter,
//                shutdownPrinter,
//                startupPrinter,
//                reprocessJob,
//                cancelCurrentJob,
//                suspendCurrentJob,
//                resumeJob,
//                promoteJob,
//                scheduleJobAfter,
//                cancelDocument,
//                getDocumentAttributes,
//                getDocuments,
//                setDocumentAttributes,
//                cancelJobs,
//                cancelMyJobs,
//                resubmitJob,
//                closeJob,
//                identifyPrinter,
//                validateDocument,
//                addDocumentImages,
//                acknowledgeDocument,
//                acknowledgeIdentifyPrinter,
//                acknowledgeJob,
//                fetchDocument,
//                fetchJob,
//                getOutputDeviceAttributes,
//                updateActiveJobs,
//                deregisterOutputDevice,
//                updateDocumentStatus,
//                updateJobStatus,
//                updateOutputDeviceAttributes,
//                getNextDocumentData,
//                allocatePrinterResources,
//                createPrinter,
//                deallocatePrinterResources,
//                deletePrinter,
//                getPrinters,
//                shutdownOnePrinter,
//                startupOnePrinter,
//                cancelResource,
//                createResource,
//                installResource,
//                sendResourceData,
//                setResourceAttributes,
//                createResourceSubscriptions,
//                createSystemSubscriptions,
//                disableAllPrinters,
//                enableAllPrinters,
//                getSystemAttributes,
//                getSystemSupportedValues,
//                pauseAllPrinters,
//                pauseAllPrintersAfterCurrentJob,
//                registerOutputDevice,
//                restartSystem,
//                resumeAllPrinters,
//                setSystemAttributes,
//                shutdownAllPrinters,
//                startupAllPrinters,
//                getPrinterResources,
//                getUserPrinterAttributes,
//                restartOnePrinter));
        putAttribute(charsetConfigured.of(charset));
        putAttribute(charsetSupported.of(charset));
        putAttribute(naturalLanguageConfigured.of(locale));
        putAttribute(generatedNaturalLanguageSupported.of(locale));
        putAttribute(documentFormatDefault.of(defaultFormat));
        putAttribute(documentFormatSupported.of(Arrays.asList(supportedFormats)));
        putAttribute(printerInfo.of("reindeer v printer"));
        putAttribute(printerLocation.of("Liaoning"));
        putAttribute(new StringType(Tag.mimeMediaType, "document-format-preferred").of("application/pdf"));
        putAttribute(mediaTypeSupported.of("A4"));
        putAttribute(mediaSupported.of("A4"));
        putAttribute(mediaColSupported.of("A4"));
        putAttribute(documentFormatDetailsSupported.of("A4"));
        putAttribute(jobPagesPerSetSupported.of(true));
        putAttribute(jobImpressionsSupported.of(new IntRange(1, 100)));
        putAttribute(jobPasswordEncryptionSupported.unsupported());
        putAttribute(multipleDocumentHandlingSupported.of("none"));
        putAttribute(orientationRequestedSupported.of(3));


    }

    public List<Attribute<?>> getPrinterAttributes(URI uri, String user, Attribute<String> requestedAttributes) {

        List<Attribute<?>> attributes = new ArrayList<>();
        if (requestedAttributes != null) {
            for (String attributeName : requestedAttributes) {
                Attribute<?> attribute = getAttribute(attributeName);
                logger.debug("{}:{} = {}",name,attributeName,attribute);
                if (null != attribute) {
                    attributes.add(attribute);
                }
            }
        }
        attributes.addAll(getDynamicPrinterAttributes(uri, user));
        return attributes;
    }

    private List<Attribute<?>> getDynamicPrinterAttributes(URI uri, String user) {

        Attribute<?>[] attributes = {
                printerUriSupported.of(uri),
                printerIsAcceptingJobs.of(isAcceptingJobs()),
                queuedJobCount.of(handler.getQueuedJobCount(this,user)),
                printerUpTime.of(getUpTime()),
                printerCurrentTime.of(Calendar.getInstance()),
        };
        return Arrays.asList(attributes);
    }

    public List<Attribute<?>> getOperationAttributes() {
        return Arrays.asList(OPERATION_ATTRIBUTES);
    }


    private boolean isAcceptingJobs() {
        return true;
    }


    private int getUpTime() {
        return (int) ((new Date().getTime() - startTime.getTime()) / 1000);
    }

    public Date getStartTime() {
        return startTime;
    }



    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String[] getSupportedVersion() {
        return supportedVersion;
    }

    public void setSupportedVersion(String[] supportedVersion) {
        this.supportedVersion = supportedVersion;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDefaultFormat() {
        return defaultFormat;
    }

    public void setDefaultFormat(String defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    public String[] getSupportedFormats() {
        return supportedFormats;
    }

    public void setSupportedFormats(String[] supportedFormats) {
        this.supportedFormats = supportedFormats;
    }

    public PrinterState getState() {
        return state;
    }

    public void setState(PrinterState state) {
        this.state = state;
    }

    public String getStateReason() {
        return stateReason;
    }

    public void setStateReason(String stateReason) {
        this.stateReason = stateReason;
    }

    public Attribute<?>[] getOPERATION_ATTRIBUTES() {
        return OPERATION_ATTRIBUTES;
    }

    public void setOPERATION_ATTRIBUTES(Attribute<?>[] OPERATION_ATTRIBUTES) {
        this.OPERATION_ATTRIBUTES = OPERATION_ATTRIBUTES;
    }

    public String authenticator(String token) {
        return handler.authenticator(this,token);
    }


}