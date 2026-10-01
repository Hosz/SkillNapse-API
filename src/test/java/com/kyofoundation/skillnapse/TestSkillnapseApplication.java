package com.kyofoundation.skillnapse;

import org.springframework.boot.SpringApplication;

public class TestSkillnapseApplication {

	public static void main(String[] args) {
		SpringApplication.from(SkillnapseApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
