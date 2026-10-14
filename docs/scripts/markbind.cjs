const { spawnSync } = require('node:child_process');

function fail(message) {
  console.error(`Documentation build stopped: ${message}`);
  process.exit(1);
}

const java = spawnSync('java', ['-version'], { encoding: 'utf8' });
if (java.error || java.status !== 0) {
  fail('Java is required to render PlantUML diagrams. Install a JDK and try again.');
}

// PlantUML's default search can miss Homebrew's /opt/homebrew/bin/dot on macOS.
const lookup = spawnSync(process.platform === 'win32' ? 'where' : 'which', ['dot'],
  { encoding: 'utf8' });
const dotPath = process.env.GRAPHVIZ_DOT || (lookup.stdout || '').trim().split(/\r?\n/)[0];
if (!dotPath) {
  fail('Graphviz is required. On macOS run "brew install graphviz"; '
    + 'on Ubuntu run "sudo apt-get install graphviz". Then retry.');
}

const dot = spawnSync(dotPath, ['-V'], { encoding: 'utf8' });
if (dot.error || dot.status !== 0) {
  fail(`Graphviz cannot run at "${dotPath}". Check PATH or GRAPHVIZ_DOT and try again.`);
}

const result = spawnSync(process.execPath,
  [require.resolve('markbind-cli/index.js'), ...process.argv.slice(2)], {
    env: { ...process.env, GRAPHVIZ_DOT: dotPath },
    stdio: 'inherit',
  });
if (result.error) {
  fail(result.error.message);
}
process.exit(result.status === null ? 1 : result.status);
