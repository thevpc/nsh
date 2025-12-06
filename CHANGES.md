# NSH Command Additions

## Summary

Added three essential POSIX commands to NSH (Nuts Shell) to improve shell compatibility and reduce dependency on external system commands.

## Commands Implemented

### 1. `wc` - Word Count

- **File**: `nsh-lib/src/main/java/net/thevpc/nsh/cmd/impl/posix/WcCommand.java`
- **Options**: `-l` (lines), `-w` (words), `-c` (bytes), `-m` (chars), `-L` (max line length)
- **Features**: Multiple file support, stdin input, totals calculation

### 2. `touch` - File Creation & Timestamp Update

- **File**: `nsh-lib/src/main/java/net/thevpc/nsh/cmd/impl/posix/TouchCommand.java`
- **Options**: `-a` (access time), `-m` (modification time), `-c` (no-create)
- **Features**: Creates empty files, updates timestamps, multiple file support

### 3. `mv` - Move/Rename Files

- **File**: `nsh-lib/src/main/java/net/thevpc/nsh/cmd/impl/posix/MvCommand.java`
- **Options**: `-f` (force), `-i` (interactive), `-n` (no-clobber), `-v` (verbose)
- **Features**: Rename files, move to directories, multiple source files

## Files Modified

- `nsh-lib/src/main/resources/META-INF/services/net.thevpc.nsh.cmd.NshBuiltin` - Added command registrations

## Testing

All commands tested and verified working:

```bash
wc pom.xml           # ✓ 241 lines, 264 words, 10473 bytes
wc -l pom.xml        # ✓ 241 lines
touch /tmp/test.txt  # ✓ File created
mv test.txt renamed  # ✓ File renamed
```

## Impact

- Improved POSIX compliance
- Enhanced portability across systems
- Reduced reliance on external commands
- Better bash script compatibility
