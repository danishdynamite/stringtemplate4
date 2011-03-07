/*
 [The "BSD license"]
 Copyright (c) 2009 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
     derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.stringtemplate.v4.debug;

import org.stringtemplate.v4.*;
import org.stringtemplate.v4.gui.STViz;
import org.stringtemplate.v4.misc.ErrorBuffer;
import org.stringtemplate.v4.misc.ErrorManager;
import org.stringtemplate.v4.misc.MultiMap;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** To avoid polluting ST instances with debug info when not debugging.
 *  Setting debug mode in STGroup makes it create these instead of STs.
 */
public class DebugST extends ST {
	public static class State {
		/** Track all events that occur during rendering. */
		public List<InterpEvent> interpEvents = new ArrayList<InterpEvent>();
	}

	/** Record who made us? ConstructionEvent creates Exception to grab stack */
	public ConstructionEvent newSTEvent = new ConstructionEvent();

	/** Track construction-time add attribute "events"; used for ST user-level debugging */
	public MultiMap<String, AddAttributeEvent> addAttrEvents = new MultiMap<String, AddAttributeEvent>();

	//public Interpreter interp; // set when we start interpreter in inspect()

	public DebugST() {}

	public DebugST(ST proto) { super(proto); }

	@Override
	public ST add(String name, Object value) {
		if ( STGroup.debug ) {
			addAttrEvents.map(name, new AddAttributeEvent(name, value));
		}
		return super.add(name, value);
	}

// LAUNCH A WINDOW TO INSPECT TEMPLATE HIERARCHY

    public List<InterpEvent> inspect() { return inspect(Locale.getDefault()); }

    public List<InterpEvent> inspect(int lineWidth) {
		return inspect(impl.nativeGroup.errMgr, Locale.getDefault(), lineWidth);
	}

    public List<InterpEvent> inspect(Locale locale) {
		return inspect(impl.nativeGroup.errMgr, locale, STWriter.NO_WRAP);
	}

    public List<InterpEvent> inspect(ErrorManager errMgr, Locale locale, int lineWidth) {
        ErrorBuffer errors = new ErrorBuffer();
        impl.nativeGroup.setListener(errors);
        StringWriter out = new StringWriter();
        STWriter wr = new AutoIndentWriter(out);
        wr.setLineWidth(lineWidth);
        Interpreter interp = new Interpreter(groupThatCreatedThisInstance, locale);
        interp.exec(wr, this); // render and track events
        new STViz(errMgr, this, out.toString(), interp,
                  interp.getExecutionTrace(), errors.errors);
        return interp.getEvents();
    }

    // TESTING SUPPORT

    public List<InterpEvent> getEvents() { return getEvents(Locale.getDefault()); }

    public List<InterpEvent> getEvents(int lineWidth) { return getEvents(Locale.getDefault(), lineWidth); }

    public List<InterpEvent> getEvents(Locale locale) { return getEvents(locale, STWriter.NO_WRAP); }

    public List<InterpEvent> getEvents(Locale locale, int lineWidth) {
        StringWriter out = new StringWriter();
        STWriter wr = new AutoIndentWriter(out);
        wr.setLineWidth(lineWidth);
        Interpreter interp = new Interpreter(groupThatCreatedThisInstance, locale);
        interp.exec(wr, this); // render and track events
        return interp.getEvents();
    }

}
