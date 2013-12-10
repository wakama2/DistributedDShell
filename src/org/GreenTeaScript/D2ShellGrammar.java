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
import java.util.ArrayList;

import org.GreenTeaScript.D2Shell.HostManager;

public class D2ShellGrammar extends GreenTeaUtils {
	// Grammar 
	private static String CommandSymbol(String Symbol) {
		return "__$" + Symbol;
	}
	
	public static GtSyntaxTree ParseHostDeclaration(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
		/*local*/GtSyntaxTree CommandTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "location");
		/*local*/GtToken SourceToken = null;
		/*local*/GtToken Token1 = TokenContext.Next();
		TokenContext.Next(); // "="
		ArrayList<String> hosts = new ArrayList<String>();
		while(true) {
			/*local*/GtToken host = TokenContext.Next();
			if(!host.IsQuoted()) break;
			hosts.add(host.ParsedText.replace("\"", ""));
		}

		/*local*/String Command = Token1.ParsedText;

		HostManager.addHost(Command, hosts);
		NameSpace.SetSymbol(Command, NameSpace.GetSyntaxPattern("$DShell2$"), SourceToken);
		NameSpace.SetSymbol(CommandSymbol(Command), Command, null);
		return CommandTree;
	}

	public static GtSyntaxTree ParseCommand(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
		/*local*/GtSyntaxTree CommandTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "command");
		/*local*/String Command = "";
		/*local*/GtToken SourceToken = null;
		while(TokenContext.HasNext()) {
			/*local*/GtToken Token = TokenContext.Next();
			if(Token.EqualsText(",")) {
				Token.ParsedText = "";
			}
			if(Token.EqualsText("~")) {
				Token.ParsedText = DShellGrammar.GetEnv("HOME");
			}
			if(Token.IsDelim() || Token.IsIndent()) {
				break;
			}
			SourceToken = Token;
			Command += Token.ParsedText;
			if(Token.IsNextWhiteSpace()) {
				DShellGrammar.AppendCommand(NameSpace, Command, SourceToken);
				Command = "";
				if(SourceToken.IsError()) {
					CommandTree.ToError(SourceToken);
				}
			}
		}
		DShellGrammar.AppendCommand(NameSpace, Command, SourceToken);
		if(SourceToken.IsError()) {
			CommandTree.ToError(SourceToken);
		}
		return CommandTree;
	}

//ifdef JAVA
	// this is a new interface used in ImportNativeObject
	public static void ImportGrammar(GtNameSpace NameSpace, Class<?> GrammarClass) {
		/*local*/GtParserContext ParserContext = NameSpace.Context;
		NameSpace.AppendSyntax("location", LoadParseFunc2(ParserContext, GrammarClass, "ParseHostDeclaration"), null);
		NameSpace.AppendSyntax("command", LoadParseFunc2(ParserContext, GrammarClass, "ParseCommand"), null);
		
		String Command = "localhost";
		NameSpace.SetSymbol(Command, NameSpace.GetSyntaxPattern("$DShell2$"), null);
		NameSpace.SetSymbol(CommandSymbol(Command), Command, null);
	}
//endif VAJA
}
