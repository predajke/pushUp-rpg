// Mock data for Push UP RPG leaderboard
// 500 players. Deterministic via seeded PRNG so the file is stable.

(function () {
  function mulberry32(a) {
    return function () {
      a |= 0; a = (a + 0x6D2B79F5) | 0;
      let t = a;
      t = Math.imul(t ^ (t >>> 15), t | 1);
      t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }
  const rand = mulberry32(20260428);
  const pick = (arr) => arr[Math.floor(rand() * arr.length)];
  const rng = (lo, hi) => Math.floor(rand() * (hi - lo + 1)) + lo;

  // Country flags as emoji-free flag strips: ISO code + colored bars (drawn in JSX).
  // We just store ISO 2-letter codes; renderer draws the flag.
  const COUNTRIES = [
    'US','UA','PL','DE','FR','ES','IT','GB','BR','AR','MX','JP','KR','CN','TH',
    'VN','ID','PH','IN','PK','TR','EG','ZA','NG','KE','MA','SE','NO','FI','DK',
    'NL','BE','CZ','SK','HU','RO','GR','PT','IE','AT','CH','AU','NZ','CA','CL',
    'CO','PE','VE','RU','BY','KZ','UZ','GE','AM','AZ','IL','SA','AE','IR','IQ',
    'SG','MY','TW','HK','LT','LV','EE','BG','HR','RS','SI'
  ];

  const FIRST = [
    'Krak','Vor','Mor','Drag','Vyl','Tor','Gar','Bran','Skar','Eld',
    'Ash','Ven','Dur','Hex','Rav','Ulf','Bjorn','Kael','Fen','Ryn',
    'Zar','Thal','Quin','Mira','Lior','Sora','Yume','Nira','Aza','Lor',
    'Iron','Stone','Steel','Blood','Storm','Frost','Shadow','Black','Crim','Wolf',
    'Hawk','Bone','Dawn','Night','Dusk','Grim','Pale','Wild','Lone','Red'
  ];
  const LAST = [
    'mar','grim','heart','fang','blade','born','wolf','crow','axe','helm',
    'rune','seer','hand','bane','wind','reaper','hammer','smith','shade','horn',
    'fist','breaker','singer','walker','jaw','claw','spear','hide','vein','spire',
    'thorn','iron','ash','stone','dawn','night','sun','moon','star','beard'
  ];
  const SUFFIX = ['', '_69', 'XX', '777', 'TheBeast', 'Lord', '_RU', '99', '01', '2K', 'Prime'];

  function makeName() {
    const a = pick(FIRST);
    const b = pick(LAST);
    const s = rand() < 0.35 ? pick(SUFFIX) : '';
    return a + b + s;
  }

  // Top players have absurdly high stats. Falls off log-ish.
  function statsForRank(rank) {
    // Rank 1 -> highest. Rank 500 -> baseline.
    const t = 1 - Math.log(rank) / Math.log(500); // 1..0
    const noise = () => (rand() - 0.5) * 0.08;

    const totalPushUps = Math.round(
      (180000 * Math.pow(t, 1.6) + 4500) * (1 + noise())
    );
    const lvl = Math.max(1, Math.round(99 * Math.pow(t, 0.85) + rng(0, 4)));
    const res = Math.max(0, Math.round(42 * Math.pow(t, 1.2) + rng(0, 2) - 1));
    const power = Math.round(9999 * Math.pow(t, 1.1) + 120 + noise() * 600);
    const armor = Math.round(7400 * Math.pow(t, 1.15) + 80 + noise() * 500);
    const hp = Math.round(12000 * Math.pow(t, 1.05) + 220 + noise() * 700);
    const luck = Math.round(640 * Math.pow(t, 0.9) + 12 + noise() * 60);
    const ageDays = Math.max(1, Math.round(900 * Math.pow(t, 0.6) + rng(0, 30)));

    return { totalPushUps, lvl, res, power, armor, hp, luck, ageDays };
  }

  const players = [];
  for (let i = 1; i <= 500; i++) {
    players.push({
      rank: i,
      name: makeName(),
      country: pick(COUNTRIES),
      ...statsForRank(i),
    });
  }
  // Sort by totalPushUps desc to align rank with the field.
  players.sort((a, b) => b.totalPushUps - a.totalPushUps);
  players.forEach((p, i) => (p.rank = i + 1));

  // Inject "You" at rank 364
  const you = {
    rank: 364,
    name: 'YouHero',
    country: 'UA',
    isMe: true,
    ...statsForRank(364),
  };
  // Replace the player at index 363 with "you" but keep stats consistent
  players[363] = you;

  // Mark a friend pool (for Friends tab) — pseudo-random subset incl. some near you
  const friendIdx = new Set([2, 17, 44, 88, 121, 199, 247, 312, 333, 360, 363, 365, 388, 401, 442, 477]);
  players.forEach((p, i) => { p.isFriend = friendIdx.has(i); });

  window.LB_PLAYERS = players;
  window.LB_ME = you;

  // Country code -> ISO 3166-1 alpha-3 dummy nice display
  window.LB_COUNTRIES = {
    US:'United States', UA:'Ukraine', PL:'Poland', DE:'Germany', FR:'France',
    ES:'Spain', IT:'Italy', GB:'United Kingdom', BR:'Brazil', AR:'Argentina',
    MX:'Mexico', JP:'Japan', KR:'Korea', CN:'China', TH:'Thailand',
    VN:'Vietnam', ID:'Indonesia', PH:'Philippines', IN:'India', PK:'Pakistan',
    TR:'Türkiye', EG:'Egypt', ZA:'South Africa', NG:'Nigeria', KE:'Kenya',
    MA:'Morocco', SE:'Sweden', NO:'Norway', FI:'Finland', DK:'Denmark',
    NL:'Netherlands', BE:'Belgium', CZ:'Czechia', SK:'Slovakia', HU:'Hungary',
    RO:'Romania', GR:'Greece', PT:'Portugal', IE:'Ireland', AT:'Austria',
    CH:'Switzerland', AU:'Australia', NZ:'New Zealand', CA:'Canada', CL:'Chile',
    CO:'Colombia', PE:'Peru', VE:'Venezuela', RU:'Russia', BY:'Belarus',
    KZ:'Kazakhstan', UZ:'Uzbekistan', GE:'Georgia', AM:'Armenia', AZ:'Azerbaijan',
    IL:'Israel', SA:'Saudi Arabia', AE:'UAE', IR:'Iran', IQ:'Iraq',
    SG:'Singapore', MY:'Malaysia', TW:'Taiwan', HK:'Hong Kong', LT:'Lithuania',
    LV:'Latvia', EE:'Estonia', BG:'Bulgaria', HR:'Croatia', RS:'Serbia',
    SI:'Slovenia'
  };
})();
