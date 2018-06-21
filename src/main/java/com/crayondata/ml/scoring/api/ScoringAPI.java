package com.crayondata.ml.scoring.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crayondata.ml.scoring.Main;

@RestController
public class ScoringAPI 
{
	@GetMapping("/score/wine_quality_prediction")
	public String predictWineQuality(@RequestParam String fixedAcidity,
										@RequestParam String volatileAcidity,
										@RequestParam String density,
										@RequestParam String citricAcid)
	{
		Map<String, String> input = new HashMap<String, String>();
		input.put("fixed_acidity", fixedAcidity);
		input.put("volatile_acidity", volatileAcidity);
		input.put("density", density);
		input.put("citric_acid", citricAcid);
		
		return Main.predict(input);
	}
}
