package com.carenest.backend.module.growth.service;

import com.carenest.backend.module.auth.enums.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WhoGrowthCalculatorService {

    @Data
    public static class LmsParameter {
        private int age;
        @JsonProperty("L")
        private double l;
        @JsonProperty("M")
        private double m;
        @JsonProperty("S")
        private double s;

        public LmsParameter() {}

        public LmsParameter(int age, double l, double m, double s) {
            this.age = age;
            this.l = l;
            this.m = m;
            this.s = s;
        }
    }

    private Map<Integer, LmsParameter> wfaMale;
    private Map<Integer, LmsParameter> wfaFemale;
    private Map<Integer, LmsParameter> lhfaMale;
    private Map<Integer, LmsParameter> lhfaFemale;
    private Map<Integer, LmsParameter> bmiMale5to19;
    private Map<Integer, LmsParameter> bmiFemale5to19;

    @PostConstruct
    public void init() {
        try {
            wfaMale = loadCsvLmsData("growth/wfa-b-z.csv");
            wfaFemale = loadCsvLmsData("growth/wfa-g-z.csv");
            
            lhfaMale = loadCsvLmsData("growth/lfa-b-z.csv");
            lhfaMale.putAll(loadCsvLmsData("growth/hfa-b-z.csv"));

            lhfaFemale = loadCsvLmsData("growth/lfa-g-z.csv");
            lhfaFemale.putAll(loadCsvLmsData("growth/hfa-g-z.csv"));
            
            bmiMale5to19 = loadCsvLmsData("growth/bmi_male_5_19.csv");
            bmiFemale5to19 = loadCsvLmsData("growth/bmi_female_5_19.csv");

            log.info("WHO Growth Standards data loaded successfully.");
        } catch (Exception e) {
            log.error("Failed to load WHO Growth Standards data", e);
        }
    }



    private Map<Integer, LmsParameter> loadCsvLmsData(String path) throws Exception {
        Map<Integer, LmsParameter> map = new java.util.HashMap<>();
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(new ClassPathResource(path).getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    int age = Integer.parseInt(parts[0].trim());
                    double l = Double.parseDouble(parts[1].trim());
                    double m = Double.parseDouble(parts[2].trim());
                    double s = Double.parseDouble(parts[3].trim());
                    map.put(age, new LmsParameter(age, l, m, s));
                }
            }
        }
        return map;
    }

    // Formula: Z = (((X / M)^L) - 1) / (L * S)
    public double calculateZScore(double measurement, LmsParameter lms) {
        if (lms.getL() == 0) {
            return Math.log(measurement / lms.getM()) / lms.getS();
        }
        return (Math.pow(measurement / lms.getM(), lms.getL()) - 1) / (lms.getL() * lms.getS());
    }

    public double calculatePercentile(double z) {
        // Approximation of the standard normal CDF
        double p = 0.3275911;
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        
        int sign = 1;
        if (z < 0) {
            sign = -1;
        }
        
        double x = Math.abs(z) / Math.sqrt(2.0);
        double t = 1.0 / (1.0 + p * x);
        double erf = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);
        
        double cdf = 0.5 * (1.0 + sign * erf);
        return cdf * 100.0;
    }

    public LmsParameter getWfaParameter(int ageMonths, Gender gender) {
        if (gender == Gender.MALE) {
            return wfaMale != null ? wfaMale.get(ageMonths) : null;
        }
        return wfaFemale != null ? wfaFemale.get(ageMonths) : null;
    }

    public LmsParameter getLhfaParameter(int ageMonths, Gender gender) {
        if (gender == Gender.MALE) {
            return lhfaMale != null ? lhfaMale.get(ageMonths) : null;
        }
        return lhfaFemale != null ? lhfaFemale.get(ageMonths) : null;
    }

    public LmsParameter getBmiParameter(int ageMonths, Gender gender) {
        if (gender == Gender.MALE) {
            return bmiMale5to19 != null ? bmiMale5to19.get(ageMonths) : null;
        }
        return bmiFemale5to19 != null ? bmiFemale5to19.get(ageMonths) : null;
    }
}
