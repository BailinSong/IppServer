package ooo.reindeer.ipp.server;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.IppPacket;

import java.io.InputStream;

public interface IIppPrinterEventHandler {


    /**
     * 当属性集发生变化时调用此方法进行打印
     * 此方法主要用于响应属性集的更改，允许开发者在属性更新后执行特定操作，如日志记录或用户通知
     *
     * @param ippPrinter IPP打印机实例，触发属性变更的源头
     * @param oldAttribute 旧的属性对象，代表变更前的属性状态
     * @param newAttribute 新的属性对象，代表变更后的属性状态
     */
    void onPrintAttributeSet(IppPrinter ippPrinter, Attribute<?> oldAttribute,Attribute<?> newAttribute);

    /**
     * 创建打印作业
     *
     * @param ippPrinter         打印机实例，用于创建打印作业
     * @param user               用户名，用于标识创建打印作业的用户
     * @param jobName            打印作业的名称
     * @param documentFormat     文档格式，指定要打印的文档的格式
     * @param requestPacket      请求包，包含创建打印作业的请求信息
     * @param documentInputStream 文档输入流，用于读取要打印的文档数据
     * @return 打印作业的ID，用于指定要查询的作业
     */
    int createPrintJob(IppPrinter ippPrinter, String user, String jobName, String documentFormat, IppPacket requestPacket, InputStream documentInputStream);

    /**
     * 获取打印作业的状态
     *
     * @param ippPrinter 打印机实例，用于查询作业状态
     * @param user       用户名，用于标识查询作业状态的用户
     * @param jobId      打印作业的ID，用于指定要查询的作业
     * @return 返回作业状态信息，包含作业当前的状态详情
     */
    JobStateInfo getJobState(IppPrinter ippPrinter, String user, int jobId);


    /**
     * 获取某个用户的排队作业数量
     *
     * @param ippPrinter 打印机对象，用于与打印机通信
     * @param user 用户名，用于标识作业所属的用户
     * @return 返回该用户的排队作业数量
     */
    int getQueuedJobCount(IppPrinter ippPrinter, String user);

    /**
     * 认证用户身份
     *
     * @param ippPrinter 打印机对象，用于与打印机通信
     * @param token 用户的认证令牌，用于验证用户身份
     * @return 认证通过则返回用户名，否则抛出异常或返回null
     */
    String authenticator(IppPrinter ippPrinter, String token);
}
