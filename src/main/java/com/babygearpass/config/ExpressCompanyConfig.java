package com.babygearpass.config;

import com.babygearpass.dto.logistics.ExpressCompanyDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ExpressCompanyConfig {

    @Bean
    public List<ExpressCompanyDTO> expressCompanies() {
        List<ExpressCompanyDTO> companies = new ArrayList<>();

        companies.add(new ExpressCompanyDTO(
                "SF",
                "顺丰速运",
                "https://www.sf-express.com",
                "95338",
                Arrays.asList("^SF[0-9]{12}$", "^[0-9]{12}$", "^[0-9]{15}$")
        ));

        companies.add(new ExpressCompanyDTO(
                "YTO",
                "圆通速递",
                "https://www.yto.net.cn",
                "95554",
                Arrays.asList("^YT[0-9]{13}$", "^[0-9]{10}$", "^[0-9]{12}$", "^[0-9]{13}$")
        ));

        companies.add(new ExpressCompanyDTO(
                "ZTO",
                "中通快递",
                "https://www.zto.com",
                "95311",
                Arrays.asList("^ZT[0-9]{10}$", "^[0-9]{12}$", "^[0-9]{13}$", "^7[0-9]{10}$")
        ));

        companies.add(new ExpressCompanyDTO(
                "STO",
                "申通快递",
                "https://www.sto.cn",
                "95543",
                Arrays.asList("^ST[0-9]{12}$", "^[0-9]{12}$", "^[0-9]{13}$", "^[0-9]{15}$")
        ));

        companies.add(new ExpressCompanyDTO(
                "YD",
                "韵达速递",
                "https://www.yundaex.com",
                "95546",
                Arrays.asList("^YD[0-9]{13}$", "^[0-9]{13}$", "^[0-9]{15}$")
        ));

        companies.add(new ExpressCompanyDTO(
                "JD",
                "京东物流",
                "https://www.jdl.com",
                "950616",
                Arrays.asList("^JD[0-9]{13}$", "^JDV[0-9]{13}$", "^[0-9]{13}$", "^[0-9]{15}$")
        ));

        companies.add(new ExpressCompanyDTO(
                "EMS",
                "EMS邮政快递",
                "https://www.ems.com.cn",
                "11183",
                Arrays.asList("^E[A-Z]{1}[0-9]{9}CN$", "^[0-9]{13}$", "^10[0-9]{11}$", "^11[0-9]{11}$", "^9[0-9]{12}$")
        ));

        companies.add(new ExpressCompanyDTO(
                "DBL",
                "德邦快递",
                "https://www.deppon.com",
                "95353",
                Arrays.asList("^DP[0-9]{10}$", "^[0-9]{9}$", "^[0-9]{10}$", "^[0-9]{12}$")
        ));

        companies.add(new ExpressCompanyDTO(
                "HTKY",
                "百世快递",
                "https://www.800best.com",
                "95320",
                Arrays.asList("^HT[0-9]{13}$", "^[0-9]{12}$", "^[0-9]{13}$", "^B[0-9]{12}$")
        ));

        companies.add(new ExpressCompanyDTO(
                "OTHER",
                "其他快递公司",
                "",
                "",
                Arrays.asList("^[A-Z0-9]{8,20}$")
        ));

        return companies;
    }
}
