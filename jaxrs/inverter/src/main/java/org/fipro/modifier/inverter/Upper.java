package org.fipro.modifier.inverter;

import org.fipro.modifier.api.StringModifier;
import org.osgi.service.component.annotations.Component;

@Component
public class Upper implements StringModifier {

	@Override
	public String modify(String input) {
		return input.toUpperCase();
	}
}