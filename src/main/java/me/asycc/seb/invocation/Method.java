package me.asycc.seb.invocation;

@FunctionalInterface
interface Method {

	void invoke(Object parent, Object arg);

}
