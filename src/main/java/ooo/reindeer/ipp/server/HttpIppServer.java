package ooo.reindeer.ipp.server;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Status;
import com.hp.jipp.trans.IppPacketData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.hp.jipp.encoding.Tag.operationAttributes;
import static com.hp.jipp.model.Types.*;
import static org.slf4j.LoggerFactory.getLogger;

public class HttpIppServer extends HttpIppServerTransport {

    final static Logger logger = getLogger(HttpIppServer.class);

    ConcurrentMap<String, IppPrinter> printers = new ConcurrentHashMap<>();
    /**
     * 向服务器添加打印机
     *
     * @param name 打印机的名称，用作唯一标识
     * @param printer IppPrinter实例，代表要添加的打印机
     * @return 如果添加成功，返回添加的IppPrinter实例；否则返回null
     */
    public IppPrinter addPrinter(String name, IppPrinter printer) {
        // 添加打印机
        logger.info("Add Printer with IPP on ipp://<USER>:<PASSWORD>@<HOST>:{}{}/{}",port,path,name);
        return printers.put(name, printer);
    }

    /**
     * 根据名称获取打印机实例
     *
     * @param name 打印机的名称，用作唯一标识
     * @return 如果找到对应名称的打印机，返回IppPrinter实例；否则返回null
     */
    public IppPrinter getPrinter(String name) {
        return printers.get(name);
    }

    /**
     * 根据名称移除打印机
     *
     * @param name 打印机的名称，用作唯一标识
     * @return 如果成功移除，返回被移除的IppPrinter实例；否则返回null
     */
    public IppPrinter removePrinter(String name) {
        return printers.remove(name);
    }

    /**
     * 构造函数，初始化HttpIppServer
     *
     * @param path 服务器路径
     * @param port 服务器端口号
     * @throws IOException 如果初始化服务器时发生I/O错误
     */
    public HttpIppServer(String path, int port) throws IOException {
        super(path, port);
    }

    /**
     * 从URI中提取打印机名称
     *
     * @param uri 包含打印机名称的URI
     * @return 返回提取的打印机名称
     * @throws IllegalArgumentException 如果URI格式无效
     */
    private String getPrinterNameFromUri(URI uri) {
        String path = uri.getPath();
        String[] parts = path.split("/");
        if (parts.length > 2) {
            return parts[2]; // 返回 "test_pring"
        } else {
            throw new IllegalArgumentException("Invalid URI format");
        }
    }

    /**
     * 根据URI获取对应的打印机实例
     *
     * @param uri 包含打印机名称的URI
     * @return 如果找到对应的打印机，返回IppPrinter实例；否则返回null
     */
    private IppPrinter getPrinter(URI uri) {
        return printers.get(getPrinterNameFromUri(uri));
    }

    /**
     * 处理IPP数据包
     *
     * @param uri 请求的URI
     * @param ippPacketData 包含请求数据的IppPacketData对象
     * @return 返回处理后的IppPacketData对象
     * @throws IOException 如果处理过程中发生I/O错误
     */
    @NotNull
    @Override
    public IppPacketData handle(@NotNull URI uri, @NotNull IppPacketData ippPacketData) throws IOException {
        IppPrinter printer = getPrinter(uri);
        URI fullUri = ippPacketData.getPacket().getValue(operationAttributes, printerUri);
        String user = printer.authenticator(fullUri.getUserInfo());

        logger.debug("{} was called ",uri);
        logger.debug("Request: {}" ,ippPacketData);
        IppPacketData serverResponse = handleIppPacketData(uri, printer, user, ippPacketData);
        logger.debug("Response: {}" ,serverResponse);
        return serverResponse;
    }

    /**
     * 处理IPP数据包的内部方法
     *
     * @param uri 请求的URI
     * @param printer IppPrinter实例
     * @param user 用户名
     * @param data 包含请求数据的IppPacketData对象
     * @return 返回处理后的IppPacketData对象
     * @throws IOException 如果处理过程中发生I/O错误
     */
    public IppPacketData handleIppPacketData(URI uri, IppPrinter printer, String user, IppPacketData data) throws IOException {

        IppPacket responsePacket;
        if (data.getPacket().getOperation().equals(Operation.getPrinterAttributes)) {
            logger.debug("Get printer attributes");
            responsePacket = getPrinterAttributesHandle(uri, printer, user, data);
        } else if (data.getPacket().getOperation().equals(Operation.validateJob)) {
            logger.debug("Validate job");
            responsePacket = defaultHandle(printer, user, data);
        } else if (data.getPacket().getOperation().equals(Operation.printJob)) {
            logger.debug("Print job");
            responsePacket = printJobHandle(uri, printer, user, data);
        } else if (data.getPacket().getOperation().equals(Operation.getJobAttributes)) {
            logger.debug("Get job attributes");
            responsePacket = getJobAttributesHandle(uri, printer, user, data);
        } else {
            logger.debug("Unknown operation");
            responsePacket = defaultHandle(printer, user, data);
        }

        return new IppPacketData(responsePacket, data.getData());
    }

    /**
     * 默认处理方法，用于未知操作
     *
     * @param printer IppPrinter实例
     * @param user 用户名
     * @param data 包含请求数据的IppPacketData对象
     * @return 返回处理后的IppPacket对象
     */
    IppPacket defaultHandle(IppPrinter printer, String user, IppPacketData data) {

        IppPacket ippPacket = data.getPacket();

        return new IppPacket(
                printer.getVersion(),
                Status.successfulOk.getCode(),
                ippPacket.getRequestId(),
                AttributeGroup.groupOf(operationAttributes),
                AttributeGroup.groupOf(Tag.printerAttributes)
        );
    }

    /**
     * 处理获取打印机属性的操作
     *
     * @param uri 请求的URI
     * @param printer IppPrinter实例
     * @param user 用户名
     * @param requestPacketData 包含请求数据的IppPacketData对象
     * @return 返回处理后的IppPacket对象
     */
    public IppPacket getPrinterAttributesHandle(URI uri, IppPrinter printer, String user, IppPacketData requestPacketData) {

        IppPacket requestPacket = requestPacketData.getPacket();
        AttributeGroup attributeGroup = requestPacket.get(operationAttributes);
        Attribute<String> attributes = attributeGroup.get(requestedAttributes);

        return new IppPacket(
                printer.getVersion(),
                Status.successfulOk.getCode(),
                requestPacket.getRequestId(),
                AttributeGroup.groupOf(operationAttributes, printer.getOperationAttributes()),
                AttributeGroup.groupOf(Tag.printerAttributes, printer.getPrinterAttributes(uri, user, attributes))
        );
    }

    /**
     * 处理打印作业的操作
     *
     * @param uri 请求的URI
     * @param printer IppPrinter实例
     * @param user 用户名
     * @param data 包含请求数据的IppPacketData对象
     * @return 返回处理后的IppPacket对象
     * @throws IOException 如果处理过程中发生I/O错误
     */
    public IppPacket printJobHandle(URI uri, IppPrinter printer, String user, IppPacketData data) throws IOException {

        IppPacket requestPacket = data.getPacket();

        int jobId = printer.handler.createPrintJob(printer, user, requestPacket.getString(operationAttributes, jobName), requestPacket.getString(operationAttributes, documentFormat), requestPacket, data.getData());

        return getJobAttributes(uri, printer, user, jobId, requestPacket);
    }

    /**
     * 获取作业属性
     *
     * @param uri 请求的URI
     * @param printer IppPrinter实例
     * @param user 用户名
     * @param localJobId 作业ID
     * @param requestPacket 请求的IppPacket对象
     * @return 返回包含作业属性的IppPacket对象
     */
    @NotNull
    private static IppPacket getJobAttributes(URI uri, IppPrinter printer, String user, int localJobId, IppPacket requestPacket) {
        JobStateInfo jobstateInfo = printer.handler.getJobState(printer, user, localJobId);

        Attribute<?>[] jobAttributes = {
                jobUri.of(uri.resolve(printer.getName() + "/").resolve("job/").resolve(String.valueOf(localJobId))),
                jobId.of(localJobId),
                jobstateInfo.getJobState(),
                jobstateInfo.getJobStateReason()
        };

        return new IppPacket(
                printer.getVersion(),
                Status.successfulOk.getCode(),
                requestPacket.getRequestId(),
                AttributeGroup.groupOf(Tag.jobAttributes, jobAttributes),
                AttributeGroup.groupOf(operationAttributes, printer.getOperationAttributes())
        );
    }

    /**
     * 处理获取作业属性的操作
     *
     * @param uri 请求的URI
     * @param printer IppPrinter实例
     * @param user 用户名
     * @param data 包含请求数据的IppPacketData对象
     * @return 返回处理后的IppPacket对象
     * @throws IOException 如果处理过程中发生I/O错误
     */
    public IppPacket getJobAttributesHandle(URI uri, IppPrinter printer, String user, IppPacketData data) throws IOException {

        IppPacket requestPacket = data.getPacket();

        Optional<Integer> printJobIdOpt = Optional.ofNullable(requestPacket.getValue(operationAttributes, jobId));
        int printJobId = printJobIdOpt.orElseThrow(() -> new IllegalArgumentException("Job ID not found in the request packet"));

        return getJobAttributes(uri, printer, user, printJobId, requestPacket);
    }

}


