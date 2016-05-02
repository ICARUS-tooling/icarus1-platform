
Supported document properties:
	documentId				string		identifier of the current document (no restrictions)		
	audio-file				string		file name of the *.wav file containing speech data for the current document
	audio-offset			string		timestamp of the beginning of the current document within the respective audio file

Supported sentence properties:
	sent-num				integer		index of the sentence within the enclosing document

Supported word properties (usable as head properties in coref search):
	tonal_prominence		boolean 	indicating tonal prominence on at least one syllable in the word (calculated from PaIntE)
	stress					boolean 	indicating presence of at least one stressed syllable in the word
	syllable_count			integer		number of syllables in the word
	begin_timestamp			float		begin timestamp of the first syllable in the word
	end_timestamp			float		end timestamp of the last syllable in the word
	is_lex					string		lexical information status
	is_ref					string		referential information status
	form					string
	lemma					string
	pos						string
	features				string
	speaker					string
	speaker_features		string

Supported syllable properties:
	syllable_offset			integer		character based offsets for syllables in the word
	syllable_form			string		part of the word form mapped to this syllable
	syllable_label			string		SAMPA label for the syllable
	syllable_timestamp		float		begin timestamp of the syllable
	syllable_vowel			string		phonetic vowel description
	syllable_stress			boolean
	syllable_duration		float
	vowel_duration			float
	syllable_startpitch		float
	syllable_midpitch		float
	syllable_endpitch		float
	coda_type				string
	coda_size				integer
	onset_type				string
	onset_size				integer
	phoneme_count			integer
	painte_a1				float
	painte_a2				float
	painte_b				float
	painte_c1				float
	painte_c2				float
	painte_d				float
	painte_max_c			float		maximum taken from painte_c1 and painte_c2
	
Supported header properties for the prosody reader:
	columns							detailed specification of each column in the tabular format
	documentBegin					prefix to be used to detect the beginning of a document
	documentEnd						prefix to be used to detect the end of a document
	sentenceBegin					prefix to be used to detect the beginning of a sentence
	sentenceEnd						prefix to be used to detect the end of a sentence
	wordBegin						prefix to be used to detect the beginning of a word
	wordEnd							prefix to be used to detect the end of a word
	separator						pattern to be used for splitting the column data in a row of text

	skipEmptyLines					flag to indicate whether empty lines should be skipped entirely without even considering above delimiter properties
	syllableOffsetsFromSampa		flag to indicate whether to create syllable offsets from sampa annotations
	localSampaRulesFile				name or path of a sampa rules file, relative to the /data folder
	markAccentOnWords				flag to indicate whether words should receive a marker when they are found to host an accented syllable
	onlyConsiderStressedSylables	flag to indicate that only syllables should be considered for the above accent marking when they are also stressed
	accentExcursion					excursion (min value of either c1 or c2) to be used as threshold when performing above mentioned accent marking
	
Column format for the prosody reader's header section:
	[property_key,type,level,role,separator]

	property_key			the property key the content of a column will be stored with (it is recommended to stick to the property names defined in the beginning of this document!)
	type					value type of the column content (int,integer,float,double,bool,boolean,bit,string)
	level					level within the active document-set the content of the column is meant to annotate (syl,word,sentence,sent,document,doc,document-set) 
	role					optional hint that the content of a column should either be used as delimiter (DEL) to detect the end or beginning of data points or as aggregator (AGG) that hosts multiple data points of the next lower level (currently only supported on the word level, meaning the aggregated content is meant to be projected onto the syllable level)
	separator				optional info on how to split aggregated content in case the column is marked as aggregator (AGG)

Special constants usable as property values in the prosody reader header section:
	TAB						tab symbol (\t)
	SPACE					space symbol ( )
	NEWLINE					matches an empty line
	NEWLINES				matches an arbitrary number of consecutive empty lines
	WHITESPACE				matches an arbitrary sequence of whitespace characters (space, \t, etc...)