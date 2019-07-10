package cn.itlou.demo;

import java.util.Objects;

/**
 * 链表形式，参考netty的实现
 */
public class PipelineDemo {

    /**
     * 初始化的时候造一个head，作为责任链的开始，但是并没有具体的处理
     */
    public HandlerChainContext head = new HandlerChainContext(new AbstractHandler() {
        @Override
        void doHandler(HandlerChainContext handlerChainContext, Object arg0) {
            handlerChainContext.runNext(arg0);
        }
    });

    public void requestProcess(Object arg0){
        this.head.handler(arg0);
    }

    public void addLast(AbstractHandler handler){
        HandlerChainContext context = head;
        while (context.next != null) {
            context = context.next;
        }
        context.next = new HandlerChainContext(handler);
    }

    public static void main(String[] args) {
        PipelineDemo pipelineDemo = new PipelineDemo();
        pipelineDemo.addLast(new Handler1());
        pipelineDemo.addLast(new Handler3());
        pipelineDemo.addLast(new Handler3());
        pipelineDemo.addLast(new Handler2());
        pipelineDemo.addLast(new Handler1());
        //发起请求
        pipelineDemo.requestProcess("火车呜呜呜呜呜~ ~~ ~~");
    }

}

/**
 * handler上下文，主要负责维护链，和链的执行
 */
class HandlerChainContext{
    HandlerChainContext next;
    AbstractHandler handler;
    public HandlerChainContext(AbstractHandler handler){
        this.handler = handler;
    }
    void handler(Object arg0){
        this.handler.doHandler(this, arg0);
    }
    /**
     * 继续执行下一个
     */
    void runNext(Object arg0){
        if (this.next != null) {
            this.next.handler(arg0);
        }
    }
}

abstract class AbstractHandler{
    /**
     * 处理器，这个处理器就做一件事，在传入字符串中增加一个尾巴
     */
    abstract void doHandler(HandlerChainContext handlerChainContext, Object arg0);
}

class Handler1 extends AbstractHandler{
    @Override
    void doHandler(HandlerChainContext handlerChainContext, Object arg0) {
        arg0 = arg0.toString() + "..handler1的小尾巴.....";
        System.out.println("我是handler1的实例，我在处理：" + arg0);
        //继续执行下一个
        handlerChainContext.runNext(arg0);
    }
}

class Handler2 extends AbstractHandler{
    @Override
    void doHandler(HandlerChainContext handlerChainContext, Object arg0) {
        arg0 = arg0.toString() + "..handler2的小尾巴.....";
        System.out.println("我是handler2的实例，我在处理：" + arg0);
        //继续执行下一个
        handlerChainContext.runNext(arg0);
    }
}

class Handler3 extends AbstractHandler{
    @Override
    void doHandler(HandlerChainContext handlerChainContext, Object arg0) {
        arg0 = arg0.toString() + "..handler3的小尾巴.....";
        System.out.println("我是handler3的实例，我在处理：" + arg0);
        //继续执行下一个
        handlerChainContext.runNext(arg0);
    }
}
