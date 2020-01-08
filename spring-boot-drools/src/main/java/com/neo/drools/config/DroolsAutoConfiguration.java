package com.neo.drools.config;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.spring.KModuleBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;


@Configuration
public class DroolsAutoConfiguration {
    
    private static final String RULES_PATH = "rules/";
    
    @Bean
    @ConditionalOnMissingBean(KieFileSystem.class)
    public KieFileSystem kieFileSystem() throws IOException {
        KieFileSystem kieFileSystem = getKieServices().newKieFileSystem();
        for (Resource file : getRuleFiles()) {
            kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH + file.getFilename(), "UTF-8"));
        }
        return kieFileSystem;
    }

    private Resource[] getRuleFiles() throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        return resourcePatternResolver.getResources("classpath*:" + RULES_PATH + "**/*.*");
    }
    
    @Bean
    @ConditionalOnMissingBean(KieContainer.class)
    public KieContainer kieContainer() throws IOException {
    	//KieRepository：是一个KieModule的仓库，包含了所有的KieModule描述，用一个ReleaseId做区分
        final KieRepository kieRepository = getKieServices().getRepository();
        //KieModule：是一个包含了多个kiebase定义的容器。一般用kmodule.xml来表示
        kieRepository.addKieModule(new KieModule() {
            public ReleaseId getReleaseId() {
                return kieRepository.getDefaultReleaseId();
            }
        });
        
        //KieFileSystem：一个完整的文件系统,包括资源和组织结构
        
        //KieBuilder：当把所有的规则文件添加到KieFileSystem中后，通过把KieFileSystem传递给一个KieBuilder，可以构建出这个虚拟文件系统。
        KieBuilder kieBuilder = getKieServices().newKieBuilder(kieFileSystem());
        //其中有个buildAll（）方法，会在构建好虚拟文件系统后，自动去构建KieModule
        kieBuilder.buildAll();
        //KieContainer就是一个KieBase的容器，可以根据kmodule.xml 里描述的KieBase信息来获取具体的KieSession
        KieContainer kieContainer=getKieServices().newKieContainer(kieRepository.getDefaultReleaseId());
        return kieContainer;
    }
    
    
    //KieServices：kie整体的入口,可以用来创建Container,resource,fileSystem等
    private KieServices getKieServices() {
        return KieServices.Factory.get();
    }
    
    
    /**
     * KieBase就是一个知识仓库，包含了若干的规则、流程、方法等，在Drools中主要就是规则和方法，KieBase本身并不包含运行时的数据之类的，
     * 如果需要执行规则KieBase中的规则的话，就需要根据KieBase创建KieSession
     * @return
     * @throws IOException
     */
    @Bean
    @ConditionalOnMissingBean(KieBase.class)
    public KieBase kieBase() throws IOException {
        return kieContainer().getKieBase();
    }
    
    /**
     * KieSession就是一个跟Drools引擎打交道的会话，其基于KieBase创建，它会包含运行时数据，包含“事实 Fact”，并对运行时数据事实进行规则运算
     * @return
     * @throws IOException
     */
    @Bean
    @ConditionalOnMissingBean(KieSession.class)
    public KieSession kieSession() throws IOException {
    	KieSession kieSession = kieContainer().newKieSession();
        return kieSession;
    }

    @Bean
    @ConditionalOnMissingBean(KModuleBeanFactoryPostProcessor.class)
    public KModuleBeanFactoryPostProcessor kiePostProcessor() {
        return new KModuleBeanFactoryPostProcessor();
    }
}
