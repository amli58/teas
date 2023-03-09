package com.ruoyi.gyl;

import com.ruoyi.gyl.domain.Customer;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

    /**
     * @author qxy
     * @date 2020/11/11 11:31 上午
     */
    public class DroolTest {

        public static void main(String[] args) {
            KieServices ks = KieServices.Factory.get();
            KieContainer kc = ks.getKieClasspathContainer();
            KieSession ksession = kc.newKieSession("test-rulesKS");
            Customer customer1 = new Customer();
            customer1.setAge(1);
            customer1.setName("李三");
            Customer customer2 = new Customer();
            customer2.setAge(18);
            customer2.setName("李四");
            ksession.insert(customer1);
            ksession.insert(customer2);
            ksession.fireAllRules();
            ksession.dispose();
        }
    }

