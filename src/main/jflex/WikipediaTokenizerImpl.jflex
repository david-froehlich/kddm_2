/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kddm2.lucene;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.util.LinkedList;

/**
 * JFlex-generated tokenizer that is aware of Wikipedia syntax.
 */
@SuppressWarnings("fallthrough")
%%

%class CustomWikipediaTokenizerImpl
%unicode
%integer
%function getNextToken
%pack
%char
%buffer 4096

%{

public static final int ALPHANUM          = CustomWikipediaTokenizer.ALPHANUM_ID;
public static final int APOSTROPHE        = CustomWikipediaTokenizer.APOSTROPHE_ID;
public static final int ACRONYM           = CustomWikipediaTokenizer.ACRONYM_ID;
public static final int COMPANY           = CustomWikipediaTokenizer.COMPANY_ID;
public static final int EMAIL             = CustomWikipediaTokenizer.EMAIL_ID;
public static final int HOST              = CustomWikipediaTokenizer.HOST_ID;
public static final int NUM               = CustomWikipediaTokenizer.NUM_ID;
public static final int CJ                = CustomWikipediaTokenizer.CJ_ID;
public static final int INTERNAL_LINK     = CustomWikipediaTokenizer.INTERNAL_LINK_ID;
public static final int EXTERNAL_LINK     = CustomWikipediaTokenizer.EXTERNAL_LINK_ID;
public static final int CITATION          = CustomWikipediaTokenizer.CITATION_ID;
public static final int CATEGORY          = CustomWikipediaTokenizer.CATEGORY_ID;
public static final int BOLD              = CustomWikipediaTokenizer.BOLD_ID;
public static final int ITALICS           = CustomWikipediaTokenizer.ITALICS_ID;
public static final int BOLD_ITALICS      = CustomWikipediaTokenizer.BOLD_ITALICS_ID;
public static final int HEADING           = CustomWikipediaTokenizer.HEADING_ID;
public static final int SUB_HEADING       = CustomWikipediaTokenizer.SUB_HEADING_ID;
public static final int EXTERNAL_LINK_URL = CustomWikipediaTokenizer.EXTERNAL_LINK_URL_ID;
public static final int INTERNAL_LINK_TARGET     = CustomWikipediaTokenizer.INTERNAL_LINK_TARGET_ID;


private int currentTokType;
private int numBalanced = 0;
private int positionInc = 1;
private int numLinkToks = 0;
//Anytime we start a new on a Wiki reserved token (category, link, etc.) this value will be 0, otherwise it will be the number of tokens seen
//this can be useful for detecting when a new reserved token is encountered
//see https://issues.apache.org/jira/browse/LUCENE-1133
private int numWikiTokensSeen = 0;

public static final String [] TOKEN_TYPES = CustomWikipediaTokenizer.TOKEN_TYPES;

/**
Returns the number of tokens seen inside a category or link, etc.
@return the number of tokens seen inside the context of wiki syntax.
**/
public final int getNumWikiTokensSeen(){
  return numWikiTokensSeen;
}

public final int yychar()
{
    return yychar;
}

public final int getPositionIncrement(){
  return positionInc;
}

private final LinkedList<Integer> states = new LinkedList();
private final LinkedList<Integer> tokenTypes = new LinkedList();

private void yypushstate(int state) {
    states.addFirst(yystate());
    tokenTypes.addFirst(currentTokType);
    yybegin(state);
}
private void yypopstate() {
    final int state = states.removeFirst();
    currentTokType = tokenTypes.removeFirst();
    yybegin(state);
}

/**
 * Fills Lucene token with the current token text.
 */
final void getText(CharTermAttribute t) {
  t.copyBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}

final int setText(StringBuilder buffer){
  int length = zzMarkedPos - zzStartRead;
  buffer.append(zzBuffer, zzStartRead, length);
  return length;
}

final void reset() {
  currentTokType = 0;
  numBalanced = 0;
  positionInc = 1;
  numLinkToks = 0;
  numWikiTokensSeen = 0;
  states.clear();
}


%}

// basic word: a sequence of digits & letters
ALPHANUM   = ({LETTER}|{DIGIT}|{KOREAN})+

// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possesives
APOSTROPHE =  {ALPHA} ("'" {ALPHA})+

// acronyms: U.S.A., I.B.M., etc.
// use a post-filter to remove dots
ACRONYM    =  {ALPHA} "." ({ALPHA} ".")+

// company names like AT&T and Excite@Home.
COMPANY    =  {ALPHA} ("&"|"@") {ALPHA}

// email addresses
EMAIL      =  {ALPHANUM} (("."|"-"|"_") {ALPHANUM})* "@" {ALPHANUM} (("."|"-") {ALPHANUM})+

// hostname
HOST       =  {ALPHANUM} ((".") {ALPHANUM})+

// floating point, serial, model numbers, ip addresses, etc.
// every other segment must have at least one digit
NUM        = ({ALPHANUM} {P} {HAS_DIGIT}
           | {DIGIT}+ {P} {DIGIT}+
           | {HAS_DIGIT} {P} {ALPHANUM}
           | {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+
           | {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {ALPHANUM} {P} {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {HAS_DIGIT} {P} {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+)

TAGS = "<"\/?{ALPHANUM}({WHITESPACE}*{ALPHANUM}=\"{ALPHANUM}\")*">"

// punctuation
P           = ("_"|"-"|"/"|"."|",")

// at least one digit
HAS_DIGIT  =
    ({LETTER}|{DIGIT})*
    {DIGIT}
    ({LETTER}|{DIGIT})*

ALPHA      = ({LETTER})+


LETTER     = [\u0041-\u005a\u0061-\u007a\u00c0-\u00d6\u00d8-\u00f6\u00f8-\u00ff\u0100-\u1fff\uffa0-\uffdc]

DIGIT      = [\u0030-\u0039\u0660-\u0669\u06f0-\u06f9\u0966-\u096f\u09e6-\u09ef\u0a66-\u0a6f\u0ae6-\u0aef\u0b66-\u0b6f\u0be7-\u0bef\u0c66-\u0c6f\u0ce6-\u0cef\u0d66-\u0d6f\u0e50-\u0e59\u0ed0-\u0ed9\u1040-\u1049]

KOREAN     = [\uac00-\ud7af\u1100-\u11ff]

// Chinese, Japanese
CJ         = [\u3040-\u318f\u3100-\u312f\u3040-\u309F\u30A0-\u30FF\u31F0-\u31FF\u3300-\u337f\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff\uff65-\uff9f]

WHITESPACE = \r\n | [ \r\n\t\f]

//Wikipedia
DOUBLE_BRACKET = "["{2}
DOUBLE_BRACKET_CLOSE = "]"{2}
EXTERNAL_LINK = "["
TWO_SINGLE_QUOTES = "'"{2}
CITATION = "<ref>"
CITATION_CLOSE = "</ref>"
WEIRD_CITATION = "<ref name="
WEIRD_CITATION_INLINE_CLOSE = "/>"
WEIRD_CITATION_CLOSE = "</ref>"
INFOBOX = {DOUBLE_BRACE}("I"|"i")nfobox_

WIKITABLE = "{|"
WIKITABLE_CLOSE = "|}"
DOUBLE_BRACE = "{"{2}
DOUBLE_BRACE_CLOSE = "}"{2}
PIPE = "|"
DOUBLE_EQUALS = "="{2}


%state CATEGORY_STATE
%state INTERNAL_LINK_STATE
%state INTERNAL_LINK_STATE_IGNORE
%state INTERNAL_LINK_TEXT_STATE
%state EXTERNAL_LINK_STATE
%state WIKITABLE_STATE

%state TWO_SINGLE_QUOTES_STATE
%state THREE_SINGLE_QUOTES_STATE
%state FIVE_SINGLE_QUOTES_STATE
%state DOUBLE_EQUALS_STATE
%state DOUBLE_BRACE_STATE
%state STRING

%%

<YYINITIAL>{ALPHANUM}                                                     {positionInc = 1; return ALPHANUM; }
<YYINITIAL>{APOSTROPHE}                                                   {positionInc = 1; return APOSTROPHE; }
<YYINITIAL>{ACRONYM}                                                      {positionInc = 1; return ACRONYM; }
<YYINITIAL>{COMPANY}                                                      {positionInc = 1; return COMPANY; }
<YYINITIAL>{EMAIL}                                                        {positionInc = 1; return EMAIL; }
<YYINITIAL>{NUM}                                                          {positionInc = 1; return NUM; }
<YYINITIAL>{HOST}                                                         {positionInc = 1; return HOST; }
<YYINITIAL>{CJ}                                                           {positionInc = 1; return CJ; }

//wikipedia
<YYINITIAL>{
  //First {ALPHANUM} is always the link, set positioninc to 1 for double bracket, but then inside the internal link state
  //set it to 0 for the next token, such that the link and the first token are in the same position, but then subsequent
  //tokens within the link are incremented
  {DOUBLE_BRACKET} {numWikiTokensSeen = 0; positionInc = 1; yypushstate(INTERNAL_LINK_STATE); currentTokType = INTERNAL_LINK_TARGET; break;}
  {EXTERNAL_LINK} {numWikiTokensSeen = 0; positionInc = 1; currentTokType = EXTERNAL_LINK_URL; yybegin(EXTERNAL_LINK_STATE); break;}
  {TWO_SINGLE_QUOTES} {numWikiTokensSeen = 0; positionInc = 1; if (numBalanced == 0){numBalanced++;yybegin(TWO_SINGLE_QUOTES_STATE);} else{numBalanced = 0;} break;}
  {DOUBLE_EQUALS} {numWikiTokensSeen = 0; positionInc = 1; yybegin(DOUBLE_EQUALS_STATE); break;}
  {DOUBLE_BRACE} {numWikiTokensSeen = 0; positionInc = 1; currentTokType = CITATION; yybegin(DOUBLE_BRACE_STATE); break;}
  {CITATION} {numWikiTokensSeen = 0; positionInc = 1; currentTokType = CITATION; yybegin(DOUBLE_BRACE_STATE); break;}
  {WEIRD_CITATION} {numWikiTokensSeen = 0; positionInc = 1; currentTokType = CITATION; yybegin(DOUBLE_BRACE_STATE); break;}
  {WIKITABLE}  {numWikiTokensSeen = 0; positionInc = 1; currentTokType = CITATION; yybegin(WIKITABLE_STATE); break;}
  //ignore
  [^] |{INFOBOX}                                               {numWikiTokensSeen = 0;  positionInc = 1;  break;}
}

<INTERNAL_LINK_STATE_IGNORE> {
  [^\]\[]+ {positionInc = 1; break;}
  {DOUBLE_BRACKET} {positionInc = 1; yypushstate(INTERNAL_LINK_STATE_IGNORE); break; }
  {DOUBLE_BRACKET_CLOSE} {numLinkToks = 0; positionInc=0; yypopstate(); break;}
  [^]                                               { positionInc = 1; break;}
}

<INTERNAL_LINK_STATE>{
  "{"[^|\]\[]+  {positionInc = 1; yybegin(INTERNAL_LINK_STATE_IGNORE); break;}
  [iI]"mage:"[^|\]\[]+  {positionInc = 1; yybegin(INTERNAL_LINK_STATE_IGNORE); break;}
  [fF]"ile:"[^|\]\[]+  {positionInc = 1; yybegin(INTERNAL_LINK_STATE_IGNORE); break;}
  [sS]"pecial:"[^|\]\[]+  {positionInc = 1; yybegin(INTERNAL_LINK_STATE_IGNORE); break;}
  [cC]"ategory:"[^|\]\[]+  {positionInc = 1; yybegin(INTERNAL_LINK_STATE_IGNORE); break;}
   // match link target first
  [^|\]\[]+ {positionInc = 1; numWikiTokensSeen++; return currentTokType;}
  // no push here since it is not nesting, just continuation!
  {PIPE} {positionInc = 1;currentTokType = INTERNAL_LINK; yybegin(INTERNAL_LINK_TEXT_STATE); break; }
  {DOUBLE_BRACKET_CLOSE} {numLinkToks = 0; positionInc=0; yypopstate(); break;}
  //ignore
  [^]                                               { positionInc = 1; break;}
}
<INTERNAL_LINK_TEXT_STATE> {
  [^|\]\[]+ {positionInc = 1; numWikiTokensSeen++; return currentTokType;}
  {DOUBLE_BRACKET} {positionInc = 1; yypushstate(INTERNAL_LINK_STATE); currentTokType = INTERNAL_LINK_TARGET; break; }
  {DOUBLE_BRACKET_CLOSE} {numLinkToks = 0; positionInc=0; yypopstate(); break;}
  [^]                                               { positionInc = 1; break;}
}
<EXTERNAL_LINK_STATE>{
  //increment the link token, but then don't increment the tokens after that which are still in the link
  ("http://"|"https://"){HOST}("/"?({ALPHANUM}|{P}|\?|"&"|"="|"#")*)* {positionInc = 1; numWikiTokensSeen++; yybegin(EXTERNAL_LINK_STATE); return currentTokType;}
  {ALPHANUM} {if (numLinkToks == 0){positionInc = 0;} else{positionInc = 1;} numWikiTokensSeen++; currentTokType = EXTERNAL_LINK; yybegin(EXTERNAL_LINK_STATE); numLinkToks++; return currentTokType;}
  "]" {numLinkToks = 0; positionInc = 0; yybegin(YYINITIAL);  break;}
  {WHITESPACE}                                               { positionInc = 1;  break;}
}
<CATEGORY_STATE>{
  {ALPHANUM} {yybegin(CATEGORY_STATE); numWikiTokensSeen++; return currentTokType;}
  {DOUBLE_BRACKET_CLOSE} {yybegin(YYINITIAL); break;}
  //ignore
  [^]                                               { positionInc = 1;  break;}
}
//italics
<TWO_SINGLE_QUOTES_STATE>{
  "'" {currentTokType = BOLD;  yybegin(THREE_SINGLE_QUOTES_STATE);  break;}
  "'''" {currentTokType = BOLD_ITALICS;  yybegin(FIVE_SINGLE_QUOTES_STATE);  break;}
  {ALPHANUM} {currentTokType = ITALICS; numWikiTokensSeen++;  yybegin(STRING); return currentTokType;/*italics*/}
  //we can have links inside, let those override
  {DOUBLE_BRACKET} {numWikiTokensSeen = 0; yypushstate(INTERNAL_LINK_STATE); currentTokType = INTERNAL_LINK_TARGET;  break;}
  {EXTERNAL_LINK} {currentTokType = EXTERNAL_LINK; numWikiTokensSeen = 0; yybegin(EXTERNAL_LINK_STATE);  break;}
  //ignore
  [^]                                               {  break;/* ignore */ }
}
//bold
<THREE_SINGLE_QUOTES_STATE>{
  {ALPHANUM} {yybegin(STRING); numWikiTokensSeen++; return currentTokType;}
  //we can have links inside, let those override
  {DOUBLE_BRACKET} {numWikiTokensSeen = 0; yypushstate(INTERNAL_LINK_STATE); currentTokType = INTERNAL_LINK_TARGET;  break;}
  {EXTERNAL_LINK} {currentTokType = EXTERNAL_LINK; numWikiTokensSeen = 0; yybegin(EXTERNAL_LINK_STATE);  break;}
  //ignore
  [^]                                               {  break;/* ignore */ }
}
//bold italics
<FIVE_SINGLE_QUOTES_STATE>{
  {ALPHANUM} {yybegin(STRING); numWikiTokensSeen++; return currentTokType;}
  //we can have links inside, let those override
  {DOUBLE_BRACKET} {numWikiTokensSeen = 0;  yypushstate(INTERNAL_LINK_STATE); currentTokType = INTERNAL_LINK_TARGET;  break;}
  {EXTERNAL_LINK} {currentTokType = EXTERNAL_LINK; numWikiTokensSeen = 0; yybegin(EXTERNAL_LINK_STATE);  break;}
  //ignore
  [^]                                               {  break;/* ignore */ }
}

<DOUBLE_EQUALS_STATE>{
  "=" {currentTokType = SUB_HEADING; numWikiTokensSeen = 0; yybegin(STRING);  break;}
  {ALPHANUM} {currentTokType = HEADING; yybegin(DOUBLE_EQUALS_STATE); numWikiTokensSeen++; return currentTokType;}
  {DOUBLE_EQUALS} {yybegin(YYINITIAL);  break;}
  //ignore
  [^]                                               {  break;/* ignore */ }
}

<DOUBLE_BRACE_STATE>{
  {ALPHANUM} {yybegin(DOUBLE_BRACE_STATE); numWikiTokensSeen = 0; break; }
  {DOUBLE_BRACE_CLOSE} {yybegin(YYINITIAL);  break;}
  {CITATION_CLOSE} {yybegin(YYINITIAL);  break;}
  {WEIRD_CITATION_INLINE_CLOSE} {yybegin(YYINITIAL);  break;}
  //ignore
  [^]                                               {  break;/* ignore */ }
}

<WIKITABLE_STATE>{
    {WIKITABLE_CLOSE} {yybegin(YYINITIAL);  break;}
    [^]                                               {  break;/* ignore */ }
}

<STRING> {
  "'''''" {numBalanced = 0;currentTokType = ALPHANUM; yybegin(YYINITIAL);  break;/*end bold italics*/}
  "'''" {numBalanced = 0;currentTokType = ALPHANUM;yybegin(YYINITIAL);  break;/*end bold*/}
  "''" {numBalanced = 0;currentTokType = ALPHANUM; yybegin(YYINITIAL);  break;/*end italics*/}
  "===" {numBalanced = 0;currentTokType = ALPHANUM; yybegin(YYINITIAL);  break;/*end sub header*/}
  {ALPHANUM} {yybegin(STRING); numWikiTokensSeen++; return currentTokType;/* STRING ALPHANUM*/}
  //we can have links inside, let those override
  {DOUBLE_BRACKET} {numBalanced = 0; numWikiTokensSeen = 0; yypushstate(INTERNAL_LINK_STATE); currentTokType = INTERNAL_LINK_TARGET; break;}
  {EXTERNAL_LINK} {numBalanced = 0; numWikiTokensSeen = 0; currentTokType = EXTERNAL_LINK;yybegin(EXTERNAL_LINK_STATE);  break;}
  {PIPE} {yybegin(STRING); return currentTokType;/*pipe*/}

  [^]                                              {  break;/* ignore STRING */ }
}

/** Ignore the rest */
[^] | {TAGS}                                          {  break;/* ignore */ }

