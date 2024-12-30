package ooo.reindeer.ipp.server;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.StringType;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.JobState;
import com.hp.jipp.model.JobStateReason;
import kotlin.ranges.IntRange;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hp.jipp.model.SystemConfiguredPrinters.printerInfo;
import static com.hp.jipp.model.Types.*;
import static org.slf4j.LoggerFactory.getLogger;

public class Main {

    final static Logger logger = getLogger(Main.class);

    public static void main(String[] args) throws URISyntaxException {
        IppPrinter printer = new IppPrinter("test03", "4fa29dc1-5209-4692-8a42-153d620e3c38", new IIppPrinterEventHandler() {

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

        printer.putAttribute(mediaTypeSupported.of("A4"));
        printer.putAttribute(mediaSupported.of("A4"));
        printer.putAttribute(mediaColSupported.of("A4"));
        printer.putAttribute(documentFormatDetailsSupported.of("A4"));
        printer.putAttribute(jobPagesPerSetSupported.of(true));
        printer.putAttribute(jobImpressionsSupported.of(new IntRange(1, 100)));
        printer.putAttribute(jobPasswordEncryptionSupported.unsupported());
        printer.putAttribute(multipleDocumentHandlingSupported.of("none"));
        printer.putAttribute(orientationRequestedSupported.of(3));




        try {
            HttpIppServer server = new HttpIppServer("/ipp", 8000);
            server.start();

            server.addPrinter(printer.getName(), printer);

        } catch (IOException e) {
            logger.error("IppServer start error", e);
        }
    }
}