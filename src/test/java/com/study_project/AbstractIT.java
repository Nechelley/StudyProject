package com.study_project;

import com.study_project.factory.ExecutorUserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class AbstractIT {

	@Autowired
	protected ExecutorUserFactory executorUserFactory;

}
