/* 
Accumulo Collections
Copyright 2016 Isentropy LLC
Written by Jonathan Wolff <jwolff@isentropy.com>
Isentropy specializes in big data and quantitative programming consulting,
with particular expertise in Accumulo development and installation. 
More info at http://isentropy.com.


Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.isentropy.accumulo.experimental.iterators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.accumulo.core.iterators.OptionDescriber.IteratorOptions;

import com.isentropy.accumulo.iterators.DeserializedFilter;

public class ScriptFilter extends DeserializedFilter{

	private ScriptEngine engine;
	private String language, script;

	public static final String KEY_SCRIPT_VARIABLE_NAME = "k";
	public static final String VALUE_SCRIPT_VARIABLE_NAME = "v";
	
	public static final String OPT_SCRIPTING_LANGUAGE = "lang";
	public static final String OPT_SCRIPT = "script";

	
	
	@Override
	protected boolean allow(Object key, Object value) {
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put(KEY_SCRIPT_VARIABLE_NAME, key);
		vars.put(VALUE_SCRIPT_VARIABLE_NAME, value);
		try {
			Object rslt =  engine.eval(script,new SimpleBindings(vars));
			if(rslt instanceof Boolean)
				return (Boolean) rslt;
			throw new RuntimeException("Script did not return a boolean");
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void init(SortedKeyValueIterator<Key,Value> source, Map<String,String> options, IteratorEnvironment env) throws IOException {
		super.init(source,options,env);
		language = options.get(OPT_SCRIPTING_LANGUAGE);
		script = options.get(OPT_SCRIPT);
		engine = new ScriptEngineManager().getEngineByName(language);
	}
	
	
	
	@Override
	public IteratorOptions describeOptions() {
		IteratorOptions opts = super.describeOptions();
		opts.addNamedOption(OPT_SCRIPTING_LANGUAGE, "the scripting language to use");
		opts.addNamedOption(OPT_SCRIPT, "the script to apply");
		return opts;
	}
	
	@Override
	public boolean validateOptions(Map<String,String> options) {
		if(!super.validateOptions(options))
			return false;
		return options.containsKey(OPT_SCRIPT) &&
				options.containsKey(OPT_SCRIPTING_LANGUAGE);
	}
}
