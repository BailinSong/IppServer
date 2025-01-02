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
                logger.debug("printer = {}, Attribute[{}] = {} -> {}", ippPrinter, oldAttribute != null ? oldAttribute.getName() : newAttribute != null ? newAttribute.getName() : null, oldAttribute, newAttribute);
            }

            @Override
            public int createPrintJob(IppPrinter ippPrinter, String user, String jobName, String documentFormat, IppPacket requestPacket, InputStream documentInputStream) {
                //函数入参debug 日志
                logger.debug("printer = {}, user = {}, jobName = {}, documentFormat = {}, requestPacket = {}, documentInputStream = {}", ippPrinter, user, jobName, documentFormat, requestPacket, documentInputStream);


                return JOBID.incrementAndGet();
            }

            @Override
            public JobStateInfo getJobState(IppPrinter ippPrinter, String user, int jobId) {
                //函数入参debug日志
                logger.debug("printer = {}, user = {}, jobId = {}", ippPrinter, user, jobId);
                return new JobStateInfo(JobState.completed, JobStateReason.none);

            }

            @Override
            public int getQueuedJobCount(IppPrinter ippPrinter, String user) {
                //函数入参debug日志
                logger.debug("printer = {}, user = {}", ippPrinter, user);
                return PRINTER_QUEUE_COUNT.get();
            }


            @Override
            public String authenticator(IppPrinter ippPrinter, String token) {
                //带函数名的入参debug日志
                logger.info("printer = {}, token = {}", ippPrinter, token);
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

        printer.putAttribute(printerKind.of(allFieldsToKeyword(PrinterKind.class)));
        printer.putAttribute(printBaseSupported.of(allFieldsToString(PrintBase.class)));
        printer.putAttribute(printColorModeSupported.of(allFieldsToString(PrintColorMode.class)));
        printer.putAttribute(printContentOptimizeSupported.of(allFieldsToString(PrintContentOptimize.class)));
        printer.putAttribute(printerServiceType.of(PrinterServiceType.print));
        printer.putAttribute(printQualitySupported.of(PrintQuality.all.values()));
        printer.putAttribute(printScalingSupported.of(allFieldsToString(PrintScaling.class)));
        printer.putAttribute(printRenderingIntentSupported.of(allFieldsToString(PrintRenderingIntent.class)));
        printer.putAttribute(printSupports.of(PrintSupports.standard));

        printer.putAttribute(colorSupported.of(true));
        printer.putAttribute(copiesSupported.of(new IntRange(1, Integer.MAX_VALUE)));
        printer.putAttribute(coverBackSupported.of(allFieldsNamesToString(CoverBack.class)));
        printer.putAttribute(coverFrontSupported.of(allFieldsNamesToString(CoverBack.class)));
        printer.putAttribute(coverTypeSupported.of(allFieldsToString(CoverType.class)));
        printer.putAttribute(destinationAccessesSupported.of(allFieldsNamesToString(DestinationAccesses.class)));
        printer.putAttribute(finishingsSupported.of(Finishing.all.values()));
        printer.putAttribute(sidesSupported.of(allFieldsToString(Sides.class)));
        printer.putAttribute(mediaTypeSupported.of(allFieldsToKeyword(MediaType.class)));
        printer.putAttribute(mediaSupported.of(allFieldsToKeyword(Media.class)));


        printer.putAttribute(jobPagesPerSetSupported.of(true));
        printer.putAttribute(jobImpressionsSupported.of(new IntRange(1, Integer.MAX_VALUE)));
        printer.putAttribute(jobPasswordEncryptionSupported.unsupported());
        printer.putAttribute(accuracyUnitsSupported.of(allFieldsToString(AccuracyUnit.class)));
        printer.putAttribute(balingTypeSupported.of(allFieldsToKeyword(BalingType.class)));
        printer.putAttribute(balingWhenSupported.of(allFieldsToString(BalingWhen.class)));
        printer.putAttribute(bindingReferenceEdgeSupported.of(allFieldsToString(BindingReferenceEdge.class)));
        printer.putAttribute(bindingTypeSupported.of(allFieldsToString(BindingType.class)));
        printer.putAttribute(coatingSidesSupported.of(allFieldsToString(CoatingSides.class)));
        printer.putAttribute(coatingTypeSupported.of(allFieldsToKeyword(CoatingType.class)));
        printer.putAttribute(compressionSupported.of(allFieldsToString(Compression.class)));
        printer.putAttribute(coveringNameSupported.of(allFieldsToKeyword(CoveringName.class)));
        printer.putAttribute(documentFormatDetailsSupported.of(allFieldsToString(DocumentDigitalSignature.class)));
        printer.putAttribute(feedOrientationSupported.of(allFieldsToString(FeedOrientation.class)));
        printer.putAttribute(finishingTemplateSupported.of(allFieldsToKeyword(FinishingTemplate.class)));
        printer.putAttribute(feedOrientationSupported.of(allFieldsToString(FeedOrientation.class)));
        printer.putAttribute(finishingsColSupported.of(allFieldsNamesToString(FinishingsCol.class)));
        printer.putAttribute(foldingDirectionSupported.of(allFieldsToString(FoldingDirection.class)));
        printer.putAttribute(foldingReferenceEdgeSupported.of(allFieldsToString(FoldingReferenceEdge.class)));
        printer.putAttribute(impositionTemplateSupported.of(allFieldsToKeyword(ImpositionTemplate.class)));
//        printer.putAttribute(ippFeaturesSupported.of(allFieldsToString(IppFeaturesSupported.class)));
        printer.putAttribute(jobHoldUntilSupported.of(allFieldsToKeyword(JobHoldUntil.class)));
        printer.putAttribute(jobImpressionsSupported.of(new IntRange(0, Integer.MAX_VALUE)));
        printer.putAttribute(jobMediaSheetsSupported.of(new IntRange(0, Integer.MAX_VALUE)));
        printer.putAttribute(jobPagesPerSetSupported.of(true));
        printer.putAttribute(jobRetainUntilSupported.of(allFieldsToKeyword(JobRetainUntil.class)));
        printer.putAttribute(jobSettableAttributesSupported.of(allFieldsToString(JobSettableAttributesSupported.class)));
        printer.putAttribute(jobSheetsSupported.of(allFieldsToKeyword(JobSheet.class)));
        printer.putAttribute(jobSheetsColSupported.of(allFieldsNamesToString(JobSheetsCol.class)));
        printer.putAttribute(jpegFeaturesSupported.of(allFieldsToString(JpegFeaturesSupported.class)));
        printer.putAttribute(labelModeSupported.of(allFieldsToString(LabelModeConfigured.class)));
        printer.putAttribute(laminatingSidesSupported.of(allFieldsToString(LaminatingSides.class)));
        printer.putAttribute(laminatingTypeSupported.of(allFieldsToKeyword(LaminatingType.class)));
        printer.putAttribute(materialAmountUnitsSupported.of(allFieldsToString(MaterialAmountUnit.class)));
        printer.putAttribute(materialPurposeSupported.of(allFieldsToString(MaterialPurpose.class)));
        printer.putAttribute(materialRateUnitsSupported.of(allFieldsToString(MaterialRateUnit.class)));
        printer.putAttribute(materialsColSupported.of(allFieldsNamesToString(MaterialsCol.class)));
        printer.putAttribute(materialTypeSupported.of(allFieldsToString(MaterialType.class)));
        printer.putAttribute(mediaBackCoatingSupported.of(allFieldsToKeyword(MediaBackCoating.class)));
        printer.putAttribute(mediaColSupported.of(allFieldsNamesToString(MediaCol.class)));
        printer.putAttribute(mediaColorSupported.of(allFieldsToKeyword(MediaColor.class)));
        printer.putAttribute(mediaGrainSupported.of(allFieldsToKeyword(MediaGrain.class)));
        printer.putAttribute(mediaOverprintSupported.of(allFieldsNamesToString(MediaOverprint.class)));
        printer.putAttribute(mediaPrePrintedSupported.of(allFieldsToKeyword(MediaPrePrinted.class)));
        printer.putAttribute(mediaRecycledSupported.of(allFieldsToKeyword(MediaRecycled.class)));
        printer.putAttribute(mediaSourceSupported.of(allFieldsToKeyword(MediaSource.class)));
        printer.putAttribute(mediaToothSupported.of(allFieldsToKeyword(MediaTooth.class)));
        printer.putAttribute(mediaTrackingSupported.of(allFieldsToString(MediaTracking.class)));
        printer.putAttribute(multipleDocumentHandlingSupported.of(allFieldsToString(MultipleDocumentHandling.class)));
        printer.putAttribute(multipleObjectHandlingSupported.of(allFieldsToString(MultipleObjectHandling.class)));
        printer.putAttribute(pageDeliverySupported.of(allFieldsToString(PageDelivery.class)));
        printer.putAttribute(proofPrintSupported.of(allFieldsNamesToString(ProofPrint.class)));
        printer.putAttribute(punchingReferenceEdgeSupported.of(allFieldsToString(PunchingReferenceEdge.class)));
        printer.putAttribute(separatorSheetsSupported.of(allFieldsNamesToString(SeparatorSheets.class)));

        printer.putAttribute(stitchingMethodSupported.of(allFieldsToString(StitchingMethod.class)));
        printer.putAttribute(stitchingReferenceEdgeSupported.of(allFieldsToString(StitchingReferenceEdge.class)));
        printer.putAttribute(stitchingAngleSupported.of(new IntRange(0, Integer.MAX_VALUE)));
        printer.putAttribute(stitchingLocationsSupported.of(new IntRange(0, Integer.MAX_VALUE)));
        printer.putAttribute(stitchingOffsetSupported.of(new IntRange(0, Integer.MAX_VALUE)));

        printer.putAttribute(trimmingReferenceEdgeSupported.of(allFieldsToString(TrimmingReferenceEdge.class)));
        printer.putAttribute(trimmingTypeSupported.of(allFieldsToString(TrimmingType.class)));
        printer.putAttribute(trimmingWhenSupported.of(allFieldsToString(TrimmingWhen.class)));
        printer.putAttribute(xImagePositionSupported.of(allFieldsToString(XImagePosition.class)));
        printer.putAttribute(yImagePositionSupported.of(allFieldsToString(YImagePosition.class)));
        printer.putAttribute(xImageShiftSupported.of(new IntRange(0, Integer.MAX_VALUE)));
        printer.putAttribute(yImageShiftSupported.of(new IntRange(0, Integer.MAX_VALUE)));

        printer.putAttribute(numberUpSupported.of(new IntRange(1, 6)));
        printer.putAttribute(presentationDirectionNumberUpSupported.of(allFieldsToString(PresentationDirectionNumberUp.class)));
        printer.putAttribute(printerResolutionSupported.of(
                new Resolution(300, 300, ResolutionUnit.dotsPerInch),
                new Resolution(600, 600, ResolutionUnit.dotsPerInch),
                new Resolution(1200, 1200, ResolutionUnit.dotsPerInch),
                new Resolution(2400, 2400, ResolutionUnit.dotsPerInch),
                new Resolution(4800, 4800, ResolutionUnit.dotsPerInch),
                new Resolution(9600, 9600, ResolutionUnit.dotsPerInch)));
        printer.putAttribute(mediaLeftMarginSupported.of(10));
        printer.putAttribute(mediaRightMarginSupported.of(10));
        printer.putAttribute(mediaTopMarginSupported.of(10));
        printer.putAttribute(mediaBottomMarginSupported.of(10));


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

    public static List<String> allFieldsNamesToString(Class<?> clazz) {
        return Arrays.stream(clazz.getFields())
                .filter(field -> {
                    try {
                        return (field.get(null) instanceof AttributeType) && java.lang.reflect.Modifier.isStatic(field.getModifiers()) && !field.getName().equals("Types");
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(field -> {
                    try {
                        return ((AttributeType<?>) field.get(null)).getName();
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}