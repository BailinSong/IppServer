package ooo.reindeer.ipp.server;

import com.hp.jipp.encoding.*;
import com.hp.jipp.model.*;
import kotlin.ranges.IntRange;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.hp.jipp.model.MediaType.stationery;
import static com.hp.jipp.model.SystemConfiguredPrinters.printerInfo;
import static com.hp.jipp.model.Types.*;
import static org.slf4j.LoggerFactory.getLogger;

public class Main {

    final static Logger logger = getLogger(Main.class);

    public static void main(String[] args) throws URISyntaxException {
        IppPrinter printer = new IppPrinter("test03", "urn:uuid:4fa29dc1-5209-4692-8a42-153d620e3c38", new IIppPrinterEventHandler() {

            final Logger logger = getLogger(IIppPrinterEventHandler.class);

            //此id应有业务层管理，此处实现仅做演示用
            AtomicInteger JOBID = new AtomicInteger(0);
            //此id应有业务层管理，此处实现仅做演示用
            AtomicInteger PRINTER_QUEUE_COUNT = new AtomicInteger(0);


            @Override
            public void onPrintAttributeSet(IppPrinter ippPrinter, Attribute<?> oldAttribute, Attribute<?> newAttribute) {
                logger.debug("printer = {}, oldAttribute = {} -> {}",ippPrinter,oldAttribute, newAttribute);
            }

            @Override
            public int createPrintJob(IppPrinter ippPrinter, String user, String jobName, String documentFormat, IppPacket requestPacket, InputStream documentInputStream) {
                //函数入参debug 日志
                logger.debug("printer = {}, user = {}, jobName = {}, documentFormat = {}, requestPacket = {}, documentInputStream = {}", ippPrinter,user, jobName, documentFormat, requestPacket, documentInputStream);


                return JOBID.incrementAndGet();
            }

            @Override
            public JobStateInfo getJobState(IppPrinter ippPrinter, String user, int jobId) {
                //函数入参debug日志
                logger.debug("printer = {}, user = {}, jobId = {}", ippPrinter,user, jobId);
                return new JobStateInfo(JobState.completed, JobStateReason.none);

            }

            @Override
            public int getQueuedJobCount(IppPrinter ippPrinter, String user) {
                //函数入参debug日志
                logger.debug("printer = {}, user = {}",ippPrinter, user);
                return PRINTER_QUEUE_COUNT.get();
            }


            @Override
            public String authenticator(IppPrinter ippPrinter, String token){
                //带函数名的入参debug日志
                logger.info("printer = {}, token = " ,ippPrinter, token);
                String[] userInfoString = token.split(":");

                if (userInfoString[0] != null) {
                    return userInfoString[0];
                } else {
                    throw new RuntimeException("Authentication failed");
                }

            }

        });
        //ipp://admin:123@127.0.0.1:8000/ipp/test03

        printer.putAttribute(printerInfo.of("reindeer v printer"));

        printer.putAttribute(printerLocation.of("Liaoning"));

        printer.putAttribute(new StringType(Tag.mimeMediaType, "document-format-preferred").of("application/pdf"));

        printer.putAttribute(orientationRequestedSupported.of(Orientation.all.values()));

        printer.putAttribute(colorSupported.of(true));
        printer.putAttribute(copiesSupported.of(new IntRange(1,100)));
        printer.putAttribute(coverBackSupported.of(CoverBack.media.getName(),CoverBack.mediaCol.getName(),CoverBack.coverType.getName()));
        printer.putAttribute(coverFrontSupported.of(CoverBack.media.getName(),CoverBack.mediaCol.getName(),CoverBack.coverType.getName()));
        printer.putAttribute(coverTypeSupported.of(allFieldsToString(CoverType.class)));
        printer.putAttribute(destinationAccessesSupported.of(DestinationAccesses.accessUserName.getName(),DestinationAccesses.accessPassword.getName(),DestinationAccesses.accessOauthToken.getName(),DestinationAccesses.accessPin.getName()));
        printer.putAttribute(finishingsSupported.of(Finishing.all.values()));
        printer.putAttribute(printQualitySupported.of(PrintQuality.all.values()));
        printer.putAttribute(printScalingSupported.of(allFieldsToString(PrintScaling.class)));
        printer.putAttribute(sidesSupported.of(allFieldsToString(Sides.class)));
        printer.putAttribute(mediaTypeSupported.of(allFieldsToKeyword(MediaType.class)));
        printer.putAttribute(mediaSupported.of(allFieldsToKeyword(Media.class)));
        printer.putAttribute(multipleDocumentHandlingSupported.of(allFieldsToString(MultipleDocumentHandling.class)));
        printer.putAttribute(jobPagesPerSetSupported.of(true));
        printer.putAttribute(jobImpressionsSupported.of(new IntRange(1, 100)));
        printer.putAttribute(jobPasswordEncryptionSupported.unsupported());
        printer.putAttribute(accuracyUnitsSupported.of(allFieldsToString(AccuracyUnit.class)));
        printer.putAttribute(balingTypeSupported.of(allFieldsToKeyword(BalingType.class)));
        printer.putAttribute(balingWhenSupported.of(allFieldsToString(BalingWhen.class)));
        printer.putAttribute(bindingReferenceEdgeSupported.of(allFieldsToString(BindingReferenceEdge.class)));
//        printer.putAttribute(bindingTypeSupported.of())


        try {
            HttpIppServer server = new HttpIppServer("/ipp", 8000);
            server.start();

            server.addPrinter(printer.getName(), printer);

        } catch (IOException e) {
            logger.error("IppServer start error", e);
        }
    }

    public static List<KeywordOrName> allFieldsToKeyword(Class<?> clazz) {
        return Arrays.stream(clazz.getFields())
                .filter(field -> field.getType() == String.class && java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                .map(field -> {
                    try {
                        return new KeywordOrName((String) field.get(null));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<String> allFieldsToString(Class<?> clazz) {
        return Arrays.stream(clazz.getFields())
                .filter(field -> field.getType() == String.class && java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                .map(field -> {
                    try {
                        return (String) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}