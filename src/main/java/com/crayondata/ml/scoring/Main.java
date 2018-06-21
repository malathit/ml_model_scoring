package com.crayondata.ml.scoring;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.dmg.pmml.Entity;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Computable;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.HasEntityId;
import org.jpmml.evaluator.HasEntityRegistry;
import org.jpmml.evaluator.HasProbability;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.evaluator.TargetField;
import org.jpmml.model.PMMLUtil;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import com.google.common.collect.BiMap;

public class Main 
{
	public static Evaluator evaluator;
	static
	{
		evaluator = loadModel();
	}
	
	public static void main(String[] args)
	{
		Map<String, String> input = new HashMap<String, String>();
		input.put("fixed_acidity", "7");
		input.put("volatile_acidity", "0.7");
		input.put("density", "5.0001");
		input.put("citric_acid", "0.8");
		
		predict(input);
		
	}

	private static Evaluator loadModel() {
		PMML pmml = null;
		try 
		{
			pmml = PMMLUtil.unmarshal(getFile());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ModelEvaluatorFactory modelEvalFactory = ModelEvaluatorFactory.newInstance();
		Evaluator evaluator = modelEvalFactory.newModelEvaluator(pmml);
		
		evaluator.verify();
		
		return evaluator;
	}
	
	public  static String predict(Map<String, String> input)
	{
		Map<FieldName, FieldValue> arguments = new HashMap<>();
		
		List<String> response = new ArrayList<>();
		
		List<InputField> inputFieldList = evaluator.getInputFields();
		for (InputField inputField : inputFieldList) 
		{
			FieldName fieldName = inputField.getName();
			
			FieldValue fieldValue = inputField.prepare(input.get(fieldName.getValue()));
			arguments.put(fieldName, fieldValue);
		}

		Map<FieldName, ?> results = evaluator.evaluate(arguments);
		
		List<TargetField> targetFieldsList = evaluator.getTargetFields();
		for (TargetField targetField : targetFieldsList)
		{
			FieldName targetFieldName = targetField.getName();
			Object targetFieldValue = results.get(targetFieldName);
			if(targetFieldValue instanceof Computable){
				Computable computable = (Computable)targetFieldValue;

				Object unboxedTargetFieldValue = computable.getResult();
				System.out.println(targetFieldName.getValue() + " : " + unboxedTargetFieldValue);
				response.add(targetFieldName.getValue() + " : " + unboxedTargetFieldValue);
			}
			else if(targetFieldValue instanceof HasEntityId){
				HasEntityId hasEntityId = (HasEntityId)targetFieldValue;
				HasEntityRegistry<?> hasEntityRegistry = (HasEntityRegistry<?>)evaluator;
				BiMap<String, ? extends Entity> entities = hasEntityRegistry.getEntityRegistry();
				Entity winner = entities.get(hasEntityId.getEntityId());

				if(targetFieldValue instanceof HasProbability){
					HasProbability hasProbability = (HasProbability)targetFieldValue;
					Double winnerProbability = hasProbability.getProbability(winner.getId());
					System.out.println(targetFieldName.getValue() + " : " + winnerProbability);
					response.add(targetFieldName.getValue() + " : " + winnerProbability);
				}
			}
		}
		return Arrays.toString(response.toArray(new String[0]));
	}

	private static InputStream getFile() {
		try {
			ClassPathResource resource = new ClassPathResource("/models/test_model.pmml");
			return resource.getInputStream();
		
		//return new File(getClass().getClassLoader().getResource("/models/test_model.pmml").getFile());
		}
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Evaluator getEvaluator() {
		return evaluator;
	}

	public static void setEvaluator(Evaluator evaluator) {
		Main.evaluator = evaluator;
	}
}
