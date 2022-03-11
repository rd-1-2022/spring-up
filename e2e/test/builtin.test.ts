import * as fs from 'fs';
import { rmRF, mkdirP } from '@actions/io';
import 'jest-extended';
import { sleep, tempDir, cliPath } from '../src/utils';
import { Cli } from '../src/cli';

describe('builtin commands', () => {
  let cli: Cli;

  beforeEach(async () => {
    await rmRF(tempDir);
    await mkdirP(tempDir);
    expect(fs.existsSync(cliPath)).toBe(true);
  }, 300000);

  afterEach(async () => {
    try {
      await rmRF(tempDir);
    } catch {
      console.log('Failed to remove test directories');
    }
    cli?.dispose();
  }, 100000);

  it('version returns info', async () => {
    cli = new Cli({
      command: cliPath,
      options: ['version']
    });

    cli.run();
    await sleep(1000);
    const screen = cli.screen();
    const expected = [expect.stringMatching('Build Version'), expect.stringMatching('Git Short Commit Id')];
    expect(screen).toEqual(expect.arrayContaining(expected));
  });
});
