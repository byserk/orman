package org.orman.mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides method to convert given name to a physical name using
 * some {@link PhysicalNamingPolicy}.
 * 
 * @author ahmet alp balkan <ahmetalpbalkan@gmail.com>
 *
 */
public class PhysicalNameGenerator {
	
	/**
	 * Formats given <code>originalName</code> to some other
	 * physical name using given <code>policy</code>.
	 * 
	 * @param originalName of the field, table or anything else.
	 * @param policy physical naming configuration
	 * @return converted name
	 */
	public static String format(String originalName, PhysicalNamingPolicy policy){
		if (!policy.isUnderscore()){
			// do not allow underscorores.
			originalName = _toCamelCase(originalName); // remove existing _(s) and camelize.
		} else {
			// make underscores
			originalName = camelCaseTo_(originalName);
		}
		
		if(policy.isUppercase()){
			// make uppercase
			originalName = originalName.toUpperCase();
		} else {
			originalName = originalName.toLowerCase();
		}
		
		return originalName;
	}
	
	/**
	 * Converts underscores to camel case.
	 * @param s
	 * @return
	 */
	private static final String _toCamelCase(String s){
		StringBuffer sb = new StringBuffer(s);
		while(sb.indexOf("_")>-1){
			int index = sb.indexOf("_");

			sb.deleteCharAt(index);
			
			if(index < sb.length()-1){
				sb.setCharAt(index, (char) Character.toUpperCase(sb.charAt(index)));
			}
		}
		return sb.toString();
	}
	
	/**
	 * Converts camel case {@link String}s to underscored texts.
	 * 
	 * e.g. <code>URLBuilderConfiguration</code> will be <code>URL_Builder_Configuration</code>.
	 * 
	 * @param s
	 * @return
	 */
	private static final String camelCaseTo_(String s){

		List<String> parts = new ArrayList<String>();;
		
		int onUpperFor = 0;
		int firstUpper = 0;
		
		for(int i = 0; i < s.length(); i++){
			if(Character.isUpperCase(s.charAt(i))){
				if(onUpperFor == 0){
					if(i>0){
						parts.add(s.substring(firstUpper, i));
					
					}
					firstUpper = i;
				}
				onUpperFor++;
			} else {
				if (onUpperFor > 1){
					parts.add(s.substring(firstUpper, firstUpper+onUpperFor-1));
					firstUpper += onUpperFor-1;
				}
				onUpperFor=0;
			}
		}
		
		if(firstUpper<s.length()){
			parts.add(s.substring(firstUpper, s.length()));
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < parts.size(); i++){
			sb.append(parts.get(i));
			if(i != parts.size()-1) sb.append("_");
		}
		
		return sb.toString();
	}
}
