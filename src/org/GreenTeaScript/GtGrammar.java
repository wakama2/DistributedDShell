// ***************************************************************************
// Copyright (c) 2013, JST/CREST DEOS project authors. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// *  Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// *  Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// **************************************************************************

//ifdef JAVA
package org.GreenTeaScript;
//endif VAJA

public class GtGrammar extends GreenTeaUtils {
//ifdef JAVA
	public final static GtFunc LoadTokenFunc(GtParserContext ParserContext, Object Grammar, String FuncName) {
		return GreenTeaUtils.LoadTokenFunc2(ParserContext, Grammar.getClass(), FuncName);
	}
	public final static GtFunc LoadParseFunc(GtParserContext ParserContext, Object Grammar, String FuncName) {
		return GreenTeaUtils.LoadParseFunc2(ParserContext, Grammar.getClass(), FuncName);
	}
	public final static GtFunc LoadTypeFunc(GtParserContext ParserContext, Object Grammar, String FuncName) {
		return GreenTeaUtils.LoadTypeFunc2(ParserContext, Grammar.getClass(), FuncName);
	}
//endif VAJA
	public void LoadTo(GtNameSpace NameSpace) {
		/*extension*/
	}
}
