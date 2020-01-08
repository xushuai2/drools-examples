package com.neo.drools.controller;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import org.drools.core.impl.InternalKnowledgeBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderError;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.neo.drools.model.Address;
import com.neo.drools.model.Message;
import com.neo.drools.model.fact.AddressCheckResult;

/*  规则语言
	规则引擎。专门解决复杂多变的业务场景
	rule “name”	
	attributes ---->属性	
	when	
	LHS ---->条件	
	then	
	RHS---->结果
	end*/
@RequestMapping("/test")
@Controller
public class TestController {
	//kbase创建的成本较高，而ksession成本低，所以drools给kbase设计了缓存机制，在第一次启动时可能会耗费一点点时间，第二次调用时，就会从缓存中读取，直接实例化ksession。
    @Resource
    private KieContainer kieContainer;
    
    
    
    private static String rule = "";
    private static String rule2 = "";
    static {
    	rule = "package com.neo.drools\r\n";
    	rule += "import com.neo.drools.model.Message;\r\n";
    	rule += "rule \"rule1\"\r\n";
    	rule += "\twhen\r\n";
    	rule += "Message( status == 1, myMessage : msg )";
    	rule += "\tthen\r\n";
    	rule += "\t\tSystem.out.println( 1+\":\"+myMessage );\r\n";
    	rule += "end\r\n";
    	
    	rule2 = "package com.neo.drools\r\n";
    	rule += "import com.neo.drools.model.Message;\r\n";

    	rule += "rule \"rule2\"\r\n";
    	rule += "\twhen\r\n";
    	rule += "Message( status == 2, myMessage : msg )";
    	rule += "\tthen\r\n";
    	rule += "\t\tSystem.out.println( 2+\":\"+myMessage );\r\n";
    	rule += "end\r\n";

    }

	/**
	 * Drools 规则引擎 动态读取规则
	 * @return
	 */
    @ResponseBody
    @RequestMapping("/address/dynamic")
    public String dynamic(){
    	for(int i=0;i<200;i++){
    		System.out.println("*********第"+i+"次动态获取************");
    		getDynamic();
    	}
    	return "Drools 规则引擎 动态读取规则";
    }



	private void getDynamic() {
		long t1 = System.currentTimeMillis();
    	//kbase创建的成本较高，而ksession成本低，所以drools给kbase设计了缓存机制，在第一次启动时可能会耗费一点点时间，第二次调用时，就会从缓存中读取，直接实例化ksession。
    	KieSession  kSession = null;
		try {
			KnowledgeBuilder kb = KnowledgeBuilderFactory.newKnowledgeBuilder();
			//装入规则，可以装入多个
			kb.add(ResourceFactory.newByteArrayResource(rule.getBytes("utf-8")), ResourceType.DRL);
			kb.add(ResourceFactory.newByteArrayResource(rule2.getBytes("utf-8")), ResourceType.DRL);

			// 检查规则正确性
			KnowledgeBuilderErrors errors = kb.getErrors();
			for (KnowledgeBuilderError error : errors) {
				System.out.println(error);
			}
			InternalKnowledgeBase kBase = (InternalKnowledgeBase) KnowledgeBaseFactory.newKnowledgeBase();
			kBase.addKnowledgePackages(kb.getKnowledgePackages());

			kSession = kBase.newKieSession();
			Message message1 = new Message();
			message1.setStatus(1);
			message1.setMsg("hello world!");

			Message message2 = new Message();
			message2.setStatus(2);
			message2.setMsg("hi world!");

			kSession.insert(message1);
			kSession.insert(message2);
			kSession.fireAllRules();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if (kSession != null)
				kSession.dispose();
			
			long t2 = System.currentTimeMillis();
			System.out.println("耗时ms："+(t2-t1));
		}
	}
    
    
    
    @ResponseBody
    @RequestMapping("/address")
    public void test(int num){
        Address address = new Address();
        address.setPostcode(generateRandom(num));
        KieSession kieSession = kieContainer.newKieSession();
        AddressCheckResult result = new AddressCheckResult();
        kieSession.insert(address);
        kieSession.insert(result);
        //执行规则
        int ruleFiredCount = kieSession.fireAllRules();
        kieSession.destroy();
        System.out.println("触发了" + ruleFiredCount + "条规则");
        if(result.isPostCodeResult()){
            System.out.println("规则校验通过,num="+result.getNum());
        }
    }
    
    

    /**
     * 生成随机数
     * @param num
     * @return
     */
    public String generateRandom(int num) {
        String chars = "0123456789";
        StringBuffer number=new StringBuffer();
        for (int i = 0; i < num; i++) {
            int rand = (int) (Math.random() * 10);
            number=number.append(chars.charAt(rand));
        }
        return number.toString();
    }
}
