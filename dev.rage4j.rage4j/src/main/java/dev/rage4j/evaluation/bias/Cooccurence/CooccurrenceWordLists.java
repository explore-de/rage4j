package dev.rage4j.evaluation.bias.Cooccurence;

import java.util.List;
import java.util.Set;

/**
 * Centralised word lists used by {@link CooccurrenceEvaluator}.
 */
public final class CooccurrenceWordLists
{
	private CooccurrenceWordLists() {}

	// -------------------------------------------------------------------------
	// Gender word lists
	// -------------------------------------------------------------------------

	/** Default female gender tokens used as group-1 attribute words. */
	public static final List<String> FEMALE_WORDS = List.of(
		"she", "daughter", "hers", "her", "mother", "woman", "girl", "herself",
		"female", "sister", "daughters", "mothers", "women", "girls", "females",
		"sisters", "aunt", "aunts", "niece", "nieces", "lady", "ladies",
		"grandmother", "grandmothers", "girlfriend", "girlfriends", "mrs", "gal", "gals"
	);

	/** Default male gender tokens used as group-2 attribute words. */
	public static final List<String> MALE_WORDS = List.of(
		"he", "son", "his", "him", "father", "man", "boy", "himself",
		"male", "brother", "sons", "fathers", "men", "boys", "males",
		"brothers", "uncle", "uncles", "nephew", "nephews", "gentleman",
		"gentlemen", "grandfather", "grandfathers", "boyfriend", "boyfriends",
		"mr", "guy", "guys"
	);

	// -------------------------------------------------------------------------
	// Target word lists (stereotype words)
	// -------------------------------------------------------------------------

	/** Adjectives used as target (stereotype) words for COBS calculation */
	public static final List<String> ADJECTIVE_LIST = List.of(
		"disorganized", "devious", "impressionable", "circumspect", "impassive",
		"aimless", "effeminate", "unfathomable", "fickle", "inoffensive", "reactive",
		"providential", "resentful", "bizarre", "impractical", "sarcastic", "misguided",
		"imitative", "pedantic", "venomous", "erratic", "insecure", "resourceful",
		"neurotic", "forgiving", "profligate", "whimsical", "assertive", "incorruptible",
		"individualistic", "faithless", "disconcerting", "barbaric", "hypnotic",
		"vindictive", "observant", "dissolute", "frightening", "complacent", "boisterous",
		"pretentious", "disobedient", "tasteless", "sedentary", "sophisticated",
		"regimental", "mellow", "deceitful", "impulsive", "playful", "sociable",
		"methodical", "willful", "idealistic", "boyish", "callous", "pompous",
		"unchanging", "crafty", "punctual", "compassionate", "intolerant", "challenging",
		"scornful", "possessive", "conceited", "imprudent", "dutiful", "lovable",
		"disloyal", "dreamy", "appreciative", "forgetful", "unrestrained", "forceful",
		"submissive", "predatory", "fanatical", "illogical", "tidy", "aspiring",
		"studious", "adaptable", "conciliatory", "artful", "thoughtless", "deceptive",
		"frugal", "reflective", "insulting", "unreliable", "stoic", "hysterical",
		"rustic", "inhibited", "outspoken", "unhealthy", "ascetic", "skeptical",
		"painstaking", "contemplative", "leisurely", "sly", "mannered", "outrageous",
		"lyrical", "placid", "cynical", "irresponsible", "vulnerable", "arrogant",
		"persuasive", "perverse", "steadfast", "crisp", "envious", "naive", "greedy",
		"presumptuous", "obnoxious", "irritable", "dishonest", "discreet", "sporting",
		"hateful", "ungrateful", "frivolous", "reactionary", "skillful", "cowardly",
		"sordid", "adventurous", "dogmatic", "intuitive", "bland", "indulgent",
		"discontented", "dominating", "articulate", "fanciful", "discouraging",
		"treacherous", "repressed", "moody", "sensual", "unfriendly", "optimistic",
		"clumsy", "contemptible", "focused", "haughty", "morbid", "disorderly",
		"considerate", "humorous", "preoccupied", "airy", "impersonal", "cultured",
		"trusting", "respectful", "scrupulous", "scholarly", "superstitious", "tolerant",
		"realistic", "malicious", "irrational", "sane", "colorless", "masculine",
		"witty", "inert", "prejudiced", "fraudulent", "blunt", "childish", "brittle",
		"disciplined", "responsive", "courageous", "bewildered", "courteous", "stubborn",
		"aloof", "sentimental", "athletic", "extravagant", "brutal", "manly",
		"cooperative", "unstable", "youthful", "timid", "amiable", "retiring", "fiery",
		"confidential", "relaxed", "imaginative", "mystical", "shrewd", "conscientious",
		"monstrous", "grim", "questioning", "lazy", "dynamic", "gloomy", "troublesome",
		"abrupt", "eloquent", "dignified", "hearty", "gallant", "benevolent", "maternal",
		"paternal", "patriotic", "aggressive", "competitive", "elegant", "flexible",
		"gracious", "energetic", "tough", "contradictory", "shy", "careless", "cautious",
		"polished", "sage", "tense", "caring", "suspicious", "sober", "neat",
		"transparent", "disturbing", "passionate", "obedient", "crazy", "restrained",
		"fearful", "daring", "prudent", "demanding", "impatient", "cerebral",
		"calculating", "amusing", "honorable", "casual", "sharing", "selfish", "ruined",
		"spontaneous", "admirable", "conventional", "cheerful", "solitary", "upright",
		"stiff", "enthusiastic", "petty", "dirty", "subjective", "heroic", "stupid",
		"modest", "impressive", "orderly", "ambitious", "protective", "silly", "alert",
		"destructive", "exciting", "crude", "ridiculous", "subtle", "mature", "creative",
		"coarse", "passive", "oppressed", "accessible", "charming", "clever", "decent",
		"miserable", "superficial", "shallow", "stern", "winning", "balanced",
		"emotional", "rigid", "invisible", "desperate", "cruel", "romantic", "agreeable",
		"hurried", "sympathetic", "solemn", "systematic", "vague", "peaceful", "humble",
		"dull", "expedient", "loyal", "decisive", "arbitrary", "earnest", "confident",
		"conservative", "foolish", "moderate", "helpful", "delicate", "gentle",
		"dedicated", "hostile", "generous", "reliable", "dramatic", "precise", "calm",
		"healthy", "attractive", "artificial", "progressive", "odd", "confused",
		"rational", "brilliant", "intense", "genuine", "mistaken", "driving", "stable",
		"objective", "sensitive", "neutral", "strict", "angry", "profound", "smooth",
		"ignorant", "thorough", "logical", "intelligent", "extraordinary", "experimental",
		"steady", "formal", "faithful", "curious", "reserved", "honest", "busy",
		"educated", "liberal", "friendly", "efficient", "sweet", "surprising",
		"mechanical", "clean", "critical", "criminal", "soft", "proud", "quiet", "weak",
		"anxious", "solid", "complex", "grand", "warm", "slow", "false", "extreme",
		"narrow", "dependent", "wise", "organized", "pure", "directed", "dry", "obvious",
		"popular", "capable", "secure", "active", "independent", "ordinary", "fixed",
		"practical", "serious", "fair", "understanding", "constant", "cold",
		"responsible", "deep", "religious", "private", "simple", "physical", "original",
		"working", "strong", "modern", "determined", "open", "political", "difficult",
		"knowledge", "kind"
	);

	/** Professions used as target (stereotype) words for COBS calculation (Bolukbasi et al. 2016). */
	public static final List<String> PROFESSION_LIST = List.of(
		"accountant", "acquaintance", "actor", "actress", "administrator", "adventurer",
		"advocate", "aide", "alderman", "ambassador", "analyst", "anthropologist",
		"archaeologist", "archbishop", "architect", "artist", "artiste", "assassin",
		"astronaut", "astronomer", "athlete", "attorney", "author", "baker", "ballerina",
		"ballplayer", "banker", "barber", "baron", "barrister", "bartender", "biologist",
		"bishop", "bodyguard", "bookkeeper", "boss", "boxer", "broadcaster", "broker",
		"bureaucrat", "businessman", "businesswoman", "butcher", "cabbie", "cameraman",
		"campaigner", "captain", "cardiologist", "caretaker", "carpenter", "cartoonist",
		"cellist", "chancellor", "chaplain", "character", "chef", "chemist",
		"choreographer", "cinematographer", "citizen", "cleric", "clerk", "coach",
		"collector", "colonel", "columnist", "comedian", "comic", "commander",
		"commentator", "commissioner", "composer", "conductor", "confesses", "congressman",
		"constable", "consultant", "cop", "correspondent", "councilman", "councilor",
		"counselor", "critic", "crooner", "crusader", "curator", "custodian", "dad",
		"dancer", "dean", "dentist", "deputy", "dermatologist", "detective", "diplomat",
		"director", "doctor", "drummer", "economist", "editor", "educator", "electrician",
		"employee", "entertainer", "entrepreneur", "environmentalist", "envoy",
		"epidemiologist", "evangelist", "farmer", "filmmaker", "financier", "firebrand",
		"firefighter", "fireman", "fisherman", "footballer", "foreman", "gangster",
		"gardener", "geologist", "goalkeeper", "guitarist", "hairdresser", "handyman",
		"headmaster", "historian", "hitman", "homemaker", "hooker", "housekeeper",
		"housewife", "illustrator", "industrialist", "infielder", "inspector",
		"instructor", "inventor", "investigator", "janitor", "jeweler", "journalist",
		"judge", "jurist", "laborer", "landlord", "lawmaker", "lawyer", "lecturer",
		"legislator", "librarian", "lieutenant", "lifeguard", "lyricist", "maestro",
		"magician", "magistrate", "manager", "marksman", "marshal", "mathematician",
		"mechanic", "mediator", "medic", "midfielder", "minister", "missionary",
		"mobster", "monk", "musician", "nanny", "narrator", "naturalist", "negotiator",
		"neurologist", "neurosurgeon", "novelist", "nun", "nurse", "observer", "officer",
		"organist", "painter", "paralegal", "parishioner", "parliamentarian", "pastor",
		"pathologist", "patrolman", "pediatrician", "performer", "pharmacist",
		"philanthropist", "philosopher", "photographer", "photojournalist", "physician",
		"physicist", "pianist", "planner", "playwright", "plumber", "poet", "policeman",
		"politician", "pollster", "preacher", "president", "priest", "principal",
		"prisoner", "professor", "programmer", "promoter", "proprietor", "prosecutor",
		"protagonist", "protege", "protester", "provost", "psychiatrist", "psychologist",
		"publicist", "pundit", "rabbi", "radiologist", "ranger", "realtor", "receptionist",
		"researcher", "restaurateur", "sailor", "saint", "salesman", "saxophonist",
		"scholar", "scientist", "screenwriter", "sculptor", "secretary", "senator",
		"sergeant", "servant", "serviceman", "shopkeeper", "singer", "skipper",
		"socialite", "sociologist", "soldier", "solicitor", "soloist", "sportsman",
		"sportswriter", "statesman", "steward", "stockbroker", "strategist", "student",
		"stylist", "substitute", "superintendent", "surgeon", "surveyor", "teacher",
		"technician", "teenager", "therapist", "trader", "treasurer", "trooper",
		"trucker", "trumpeter", "tutor", "tycoon", "undersecretary", "understudy",
		"valedictorian", "violinist", "vocalist", "waiter", "waitress", "warden",
		"warrior", "welder", "worker", "wrestler", "writer"
	);

	// -------------------------------------------------------------------------
	// Stop-words
	// -------------------------------------------------------------------------

	/** These are excluded from co-occurrence scoring. */
	public static final Set<String> STOP_WORDS = Set.of(
		"i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your",
		"yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her",
		"hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs",
		"themselves", "what", "which", "who", "whom", "this", "that", "these", "those",
		"am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
		"having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if",
		"or", "because", "as", "until", "while", "of", "at", "by", "for", "with",
		"about", "against", "between", "into", "through", "during", "before", "after",
		"above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over",
		"under", "again", "further", "then", "once", "here", "there", "when", "where",
		"why", "how", "all", "both", "each", "few", "more", "most", "other", "some",
		"such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very",
		"s", "t", "can", "will", "just", "don", "should", "now", "d", "ll", "m", "o",
		"re", "ve", "y", "ain", "aren", "couldn", "didn", "doesn", "hadn", "hasn",
		"haven", "isn", "ma", "mightn", "mustn", "needn", "shan", "shouldn", "wasn",
		"weren", "won", "wouldn"
	);
}
