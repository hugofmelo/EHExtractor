package ufrn.dimap.lets.ehmetrics.ops;

import com.github.javaparser.resolution.UnsolvedSymbolException;

public class UnresolvableException extends UnsolvedSymbolException {

	public UnresolvableException(String name) {
		super(name);
	}

}
