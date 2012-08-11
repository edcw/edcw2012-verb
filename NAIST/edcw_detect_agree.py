#!/usr/bin/env python
# -*- coding: utf-8 -*-

__author__ = 'Yuta Hayashibe, Keisuke Sakaguchi'
__version__ = "1.0"
__descripstion__ = "This is for detecting agreement errors in edcw plain texts (edc files)  with parsing by Stanford parser."
__usage__ = "python edcw_detect_agree.py --help"

import os
import sys
enc = sys.stdin.encoding
if enc is None:
    enc = sys.getfilesystemencoding()

import corrcha.core.singleton
import jpype
import corrcha.tool.myjpype

import corrcha.corpus.sentence
import corrcha.corpus.token
import corrcha.core.parser.parser
import argparse

#arg parsing
arg_parser = argparse.ArgumentParser()
arg_parser.add_argument('-i', action='store', dest='in_dir', 
        help='input_directory', required=True)
arg_parser.add_argument('-o', action='store', dest='out_dir', 
        help='output_directory', required=True)
args = arg_parser.parse_args()

#pre-defined dictionary
conj = ['And', 'and', 'But', 'but']
numbers = ['two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine', 'ten', 'eleven',
        'twelve', 'thirteen', 'fourteen', 'fifteen', 'sixteen', 'seventenn', 'eighteen', 'nineteen',
        'twenty', 'thirty', 'forty', 'fifty', 'sixty', 'seventy', 'eighty', 'ninety']
quant = ['many', 'more', 'most', 'lot']


# check the word person
# in a causative construction (e.g. make me happy),
# a word in objective case can be a subject.
def check_person(word):
    p_1st = ['I', 'i', 'We', 'we', 'me', 'us']
    p_2nd = ['You', 'you']
    if word in p_1st:
        return 1
    elif word in p_2nd:
        return 2
    else:
        return 3

# check if a verb is copula
def is_copula(word):
    copula = ['am', 'was', 'are', 'were', 'is']
    if word in copula:
        return True
    else:
        return False

# check if a word is plural, especially for personal and demonstrative pronouns
def is_plural(word, pos):
    prp_p = ['We', 'we', 'You', 'you', 'They', 'they']
    dt_s = ['This', 'this', 'That', 'that']
    dt_p = ['These', 'these', 'Those', 'those']

    if pos == 'PRP':
        if word in prp_p:
            return True
        else:
            return False

    elif pos == 'DT':
        if word in dt_s:
            return False
        elif word in dt_p:
            return True
        else:
            pass
    
    # 'Let' makes imperative sentance.
    elif word in quant or word == 'Let':
    #elif word == 'lot':
        return True
    
    else:
        if pos == 'NNS':
            return True
        else:
            return False


# check agreement, looking at surfice form and POS of subject and verb.
def check_agree(subj, verb, subj_pos, verb_pos):

    copula_1s = ['am', 'was']
    copula_1p = ['are', 'were']
    copula_2 = ['are', 'were']
    copula_3s = ['is', 'was']
    copula_3p = ['are', 'were']
    
    if is_copula(verb):
        person = check_person(subj)
        plural = is_plural(subj, subj_pos)
        
        if check_person(subj) == 1:
            if is_plural(subj, subj_pos):
                if verb in copula_1p:
                    return True
                else:
                    return False
            else:
                if verb in copula_1s:
                    return True
                else:
                    return False
                    
        elif check_person(subj) == 2:
            if verb in copula_2:
                return True
            else:
                return False
                
        else:
            if is_plural(subj, subj_pos):
                if verb in copula_3p:
                    return True
                else:
                    return False
            else:
                if verb in copula_3s:
                    return True
                else:
                    return False
            
    else:
        if check_person(subj) < 3:
            if verb_pos != 'VBZ':
                return True
            else:
                return False
        
        else:
            if is_plural(subj, subj_pos):
                if verb_pos != 'VBZ':
                    return True
                else:
                    return False
            else:
                if verb_pos != 'VBP':
                    return True
                else:
                    return False


# stanford parser Python wrapper (by Yuta Hayashibe)
class StanfordParser(corrcha.core.parser.parser.Parser):
    __metaclass__= corrcha.core.singleton.Singleton
    PACKAGE_NAME = "edu.stanford.nlp"
    LEX_PARSER = "edu.stanford.nlp.parser.lexparser.LexicalizedParser"
    DEFAULT_OPTION = \
            ["-retainTmpSubcategories", #get best performance in recognizing temporal dependencies \
             "-makeCopulaHead", \
            ]

    def __init__(self, jar_path, pcfg_model_fname, option=DEFAULT_OPTION):
        assert type(jar_path) is unicode
        assert type(pcfg_model_fname) is unicode
        assert isinstance(option, (list, tuple))
        corrcha.tool.myjpype.addClassPath(jar_path)
        self.pcfg_model_fname =  pcfg_model_fname
        self.parser_class = jpype.JClass(self.LEX_PARSER)
        self.parser = self.parser_class.getParserFromSerializedFile(self.pcfg_model_fname)

        self.parser.setOptionFlags(option)

        self.package = jpype.JPackage(self.PACKAGE_NAME)
        tokenizerFactoryClass = self.package.process.__getattribute__("PTBTokenizer$PTBTokenizerFactory")
        self.tokenizerFactory = tokenizerFactoryClass.newPTBTokenizerFactory(True, True)

        self.puncFilter = self.package.trees.PennTreebankLanguagePack().punctuationWordRejectFilter()
        if "-makeCopulaHead" in option:
            self.headFinder = self.package.trees.SemanticHeadFinder(False)
        else:
            self.headFinder = self.package.trees.SemanticHeadFinder(True)


    def __unicode__(self):
        buf = u""
        buf += "ParserPack is %s" % self.parser.op.tlpParams.getClass()
        buf += "PARSER_TYPE : %s" % PARSER_TYPE
        buf += "PACKAGE_NAME : %s" % PACKAGE_NAME
#        self.parser.op.display()
#        print "Test parameters"
#        self.parser.op.testOptions.display();
#        self.parser.op.tlpParams.display();
        return buf

    def _parse(self, sentence):
        """
        Parses the sentence string, returning the tokens, and the parse tree as a tuple.
        """
        assert type(sentence) is unicode
        _tokenizer = self.tokenizerFactory.getTokenizer(  jpype.java.io.BufferedReader( jpype.java.io.StringReader( sentence) )  )
        tokens = _tokenizer.tokenize();
        
        if len(tokens) == 0:
            return None, tokens

        pq = self.parser.parserQuery()
        wasParsed = pq.parse(tokens)
        if not wasParsed:
            raise
        bestparse = pq.getBestParse()
        bestparse.setScore(pq.getPCFGScore() % -10000.0) #-10000 denotes unknown words
        return bestparse, tokens
    
    
    def _parse_tree(self, tree, parsed_sentence, tokenized, parentID, lastID):
        #use changeability for lastID
        assert type(tree) is jpype.JPackage('edu').stanford.nlp.trees.LabeledScoredTreeNode
        assert type(tokenized) is list
        assert type(parentID) is int
        assert type(lastID) is list
        assert type(lastID[0]) is int
        
        kids = tree.children();
        lastID[0] += 1
        this_tree_id = lastID[0]
        this_node = corrcha.corpus.token.Node(this_tree_id, tree.label().category(), parentID)
        parsed_sentence.appendNode(this_tree_id, this_node)
        if ( len(kids) == 1 and kids[0].isLeaf()):
            leaf = ( this_tree_id, kids[0].label().word() )
            tokenized.append( leaf );
        else:
            for i, kid in enumerate(kids):
                self._parse_tree(kid, parsed_sentence, tokenized, this_tree_id, lastID)

    def parse(self, sentence):
        """Parse a raw sentence with Stanford parser.
        This returns an array of tokens, a list of tuples which contain a 'tree-tag' and 'parent tree id',
        and a list of typed dependencies.
        """
        assert type(sentence) is unicode
        parsed_sentence = corrcha.corpus.sentence.ParsedSentence(sentence)

        tree, tokens = self._parse(sentence)
        if tree is not None:
            result = self.package.trees.EnglishGrammaticalStructure(tree, self.puncFilter, self.headFinder, True)
            tokenized = []
            self._parse_tree(tree, parsed_sentence, tokenized, 0, [0])
            #           #set offset
            for i, t in enumerate(tokenized):
                position = tokens[i].beginPosition()
                #tokens[i].endPosition() )
                parsed_token = corrcha.corpus.token.Token(t[0], t[1], position )
                parsed_sentence.append(parsed_token)

#            for dependency in result.typedDependenciesCollapsedTree(): #TODO enable switching?
            for dependency in result.typedDependencies():
                #http://reason.cs.uiuc.edu/mtyoung/parser/javadoc/edu/stanford/nlp/trees/TypedDependency.html
                rel = unicode(dependency.reln())
                gov = dependency.gov().index() - 1
                dep = dependency.dep().index() - 1
                
                #gov & dep ARE NOT the number of tree-id but the index of the array
                parsed_sentence.appendRelation(gov, dep, rel)

        return parsed_sentence



if __name__=='__main__':
    import sys
    options = ('-makeCopulaHead',)
    print options

    import corrcha.tool.setting
    path = corrcha.tool.setting.val['stanford']['path'] + '/'

    model_fname = path + corrcha.tool.setting.val['stanford']['model']
    jar_path = path + corrcha.tool.setting.val['stanford']['jar']
    parser = StanfordParser(jar_path, model_fname, options)

    filenames = os.listdir(args.in_dir)

    for f in filenames:
        f_new = f.split('.')[0] + '.sys'
        print "++++++++++" + f + "++++++++++"
        result_f = open(args.out_dir + f_new, 'w')
        for sent_id, line in enumerate(open(args.in_dir + f, 'r')):
            orig_sent = line
            try:

                if line.split(' ')[0] in conj:
                    ps = parser.parse(unicode(line.strip()[4:], enc)) #parsed_sentence
                else:
                    ps = parser.parse(unicode(line.strip(), enc)) #parsed_sentence
                sent_info = []
                sent_words = []
                for token in ps.getTokens():
                    word = token.getSurface()
                    relations = token.getRelations()
                    node = ps.getNode(token)
                    tag = node.getTag() 
                    sent_info.append([word, relations, tag])
                    sent_words.append(word)

                for i, token in enumerate(sent_info):
                    if len(token) == 0:
                        result_f.writelines()
                    else:
                        dep_list = token[1].values()
                        dep_key = token[1].keys()
                        
                        for key, val in zip(dep_key, dep_list):
                            if val == 'nsubj':
                                subj = sent_info[int(key)][0]
                                subj_pos = sent_info[int(key)][2]
                                verb = sent_info[i][0]
                                verb_pos = sent_info[i][2]
                                
                                
                                if int(key) != 0 and sent_info[int(key)-1][0].endswith('ing'):
                                    subj = sent_info[int(key)-1][0]
                                    subj_pos = sent_info[int(key)-1][2]
                                else:
                                    pass
                                
                                if (sent_info[int(key)+1][0].endswith('and')
                                    or sent_info[int(key)+1][0].endswith(',')):
                                    subj = sent_info[int(key)-1][0]
                                    subj_pos = 'NNS'
                                else:
                                    pass

                                if ( sent_info[int(key)][2] == 'WDT' 
                                    or sent_info[int(key)][2] == 'WP' ):
                                    subj = sent_info[int(key)-1][0]
                                    subj_pos = sent_info[int(key)-1][2]
                                else:
                                    pass

                                
                                if ( (int(key) > 0 and sent_words[int(key)-1] in numbers) 
                                    or (int(key) > 1 and sent_words[int(key)-2] in numbers) 
                                    or (int(key) > 2 and sent_words[int(key)-3] in numbers) ):
                                    subj = sent_info[int(key)][0]
                                    subj_pos = 'NNS'
                                else:
                                    pass
                                
                                
                                if not check_agree(subj, verb, subj_pos, verb_pos):
                                    orig_sent = orig_sent.replace(' '+verb+' ', ' <v_agr>'+verb+'</v_agr> ')
                                else:
                                    pass
                         
                            elif val == 'nsubjpass':
                                subj = sent_info[int(key)][0]
                                subj_pos = sent_info[int(key)][2]
                                for key_i, val_i in zip(dep_key, dep_list):
                                    if val_i == 'auxpass':
                                        verb = sent_info[int(key_i)][0]
                                        verb_pos = sent_info[int(key_i)][2]
                                    else:
                                        pass

                                if int(key) != 0 and sent_info[int(key)-1][0].endswith('ing'):
                                    subj = sent_info[int(key)-1][0]
                                    subj_pos = sent_info[int(key)-1][2]
                                else:
                                    pass
                                
                                if (sent_info[int(key)+1][0].endswith('and')
                                    or sent_info[int(key)+1][0].endswith(',')):
                                    subj = sent_info[int(key)-1][0]
                                    subj_pos = 'NNS'
                                else:
                                    pass

                                if ( sent_info[int(key)][2] == 'WDT' 
                                    or sent_info[int(key)][2] == 'WP' ):
                                    subj = sent_info[int(key)-1][0]
                                    subj_pos = sent_info[int(key)-1][2]
                                else:
                                    pass

                                if not check_agree(subj, verb, subj_pos, verb_pos):
                                    orig_sent = orig_sent.replace(' '+verb+' ', ' <v_agr>'+verb+'</v_agr> ')
                                else:
                                    pass

                            else:
                                pass

                result_f.write(orig_sent)

            except:
                raise
                sys.stderr.write("[*] Parsing Error!!\n")
            
        result_f.close()
