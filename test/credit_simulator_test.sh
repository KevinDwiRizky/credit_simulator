#!/usr/bin/env bash
set -uo pipefail

ROOT="$(cd -P "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

PASS=0
FAIL=0

ok() {
  PASS=$((PASS + 1))
  echo "  ok   - $1"
}

fail() {
  FAIL=$((FAIL + 1))
  echo "  FAIL - $1"
  if [[ -n "${2:-}" ]]; then
    echo "         $2"
  fi
}

assert_eq() {
  if [[ "$3" == "$2" ]]; then ok "$1"; else fail "$1" "harapan: [$2] hasil: [$3]"; fi
}

assert_contains() {
  if [[ "$2" == *"$3"* ]]; then ok "$1"; else fail "$1" "tidak ada [$3] di: [$2]"; fi
}

assert_not_contains() {
  if [[ "$2" != *"$3"* ]]; then ok "$1"; else fail "$1" "seharusnya tidak ada [$3] di: [$2]"; fi
}

TOOLS="$WORK/tools"
mkdir -p "$TOOLS"
for tool in env bash dirname readlink find sed head cat mkdir touch; do
  src="$(command -v "$tool" || true)"
  if [[ -n "$src" ]]; then ln -s "$src" "$TOOLS/$tool"; fi
done

FAKEBIN="$WORK/fakebin"
mkdir -p "$FAKEBIN"

cat > "$FAKEBIN/java" <<'FAKE'
if [[ "${1:-}" == "-version" ]]; then
  echo "openjdk version \"${FAKE_JAVA_VERSION:-17.0.9}\" 2023-10-17" >&2
  exit 0
fi
{
  echo "ARGS: $*"
  echo "ARGC: $#"
  echo "CWD: $PWD"
  echo "STDIN: $(cat)"
} >> "$FAKE_LOG"
exit "${FAKE_JAVA_EXIT:-0}"
FAKE

cat > "$FAKEBIN/mvn" <<'FAKE'
echo "MVN: $*" >> "$FAKE_LOG"
echo "BUILD NOISE"
if [[ "${FAKE_MVN_FAIL:-0}" == "1" ]]; then
  echo "mvn error" >&2
  exit 1
fi
mkdir -p target
echo "fake jar" > target/credit-simulator.jar
FAKE
chmod +x "$FAKEBIN/java" "$FAKEBIN/mvn"

ONLY_JAVA="$WORK/only_java"; mkdir -p "$ONLY_JAVA"; ln -s "$FAKEBIN/java" "$ONLY_JAVA/java"
ONLY_MVN="$WORK/only_mvn"; mkdir -p "$ONLY_MVN"; ln -s "$FAKEBIN/mvn" "$ONLY_MVN/mvn"

COUNTER=0

new_project() {
  COUNTER=$((COUNTER + 1))
  local dir="$WORK/project $COUNTER"
  mkdir -p "$dir/bin" "$dir/src/main"
  cp "$ROOT/credit_simulator" "$dir/credit_simulator"
  cp "$ROOT/bin/credit_simulator" "$dir/bin/credit_simulator"
  chmod +x "$dir/credit_simulator" "$dir/bin/credit_simulator"
  echo "<project/>" > "$dir/pom.xml"
  echo "class A {}" > "$dir/src/main/A.java"
  touch -t 202001010000 "$dir/pom.xml" "$dir/src/main/A.java"
  P="$(cd -P "$dir" && pwd)"
}

fresh_jar() {
  mkdir -p "$1/target"
  echo "jar" > "$1/target/credit-simulator.jar"
}

stale_jar() {
  fresh_jar "$1"
  touch -t 201901010000 "$1/target/credit-simulator.jar"
}

run() {
  local dir="$1"; shift
  local script="${SCRIPT:-$dir/credit_simulator}"
  FAKE_LOG="$WORK/log.$COUNTER.$RANDOM"
  : > "$FAKE_LOG"
  export FAKE_LOG
  OUT="$(cd "${RUN_CWD:-$dir}" && PATH="${FAKE_PATH:-$FAKEBIN}:$TOOLS" "$script" "$@" 2>"$WORK/err" <<<"${STDIN:-}")"
  CODE=$?
  ERR="$(cat "$WORK/err")"
  LOG="$(cat "$FAKE_LOG")"
}

echo "Menjalankan uji script credit_simulator"

echo "# menjalankan JAR"
new_project; fresh_jar "$P"
run "$P"
assert_eq "tanpa argumen: kode keluar 0" "0" "$CODE"
assert_contains "tanpa argumen: menjalankan JAR project (path berspasi aman)" "$LOG" "ARGS: -jar $P/target/credit-simulator.jar"$'\n'
assert_contains "tanpa argumen: aplikasi menerima tepat 2 argumen (-jar dan path)" "$LOG" "ARGC: 2"
assert_not_contains "JAR sudah baru: tidak ada build" "$LOG" "MVN:"

run "$P" input.txt
assert_contains "argumen file diteruskan ke aplikasi" "$LOG" "credit-simulator.jar input.txt"
assert_contains "dengan satu argumen: tepat 3 argumen" "$LOG" "ARGC: 3"

run "$P" "file dengan spasi.txt"
assert_contains "argumen berspasi tetap satu argumen" "$LOG" "credit-simulator.jar file dengan spasi.txt"
assert_contains "argumen berspasi: tetap tepat 3 argumen" "$LOG" "ARGC: 3"

SCRIPT="$P/bin/credit_simulator" run "$P"
assert_contains "bin/credit_simulator bisa dijalankan langsung" "$LOG" "ARGS: -jar $P/target/credit-simulator.jar"

echo "# build otomatis"
new_project
run "$P"
assert_contains "JAR belum ada: mvn dipanggil" "$LOG" "MVN: -q -B -DskipTests package"
assert_contains "setelah build aplikasi dijalankan" "$LOG" "ARGS: -jar $P/target/credit-simulator.jar"
assert_contains "pesan build tampil di stderr" "$ERR" "Membangun aplikasi"
assert_not_contains "output build tidak mengotori stdout" "$OUT" "BUILD NOISE"
if [[ "$LOG" == *"MVN:"*"ARGS:"* ]]; then ok "build berjalan sebelum aplikasi"; else fail "build berjalan sebelum aplikasi" "$LOG"; fi

new_project; stale_jar "$P"
run "$P"
assert_contains "JAR lebih lama dari source: build ulang" "$LOG" "MVN:"

new_project; fresh_jar "$P"
touch -t 202101010000 "$P/target/credit-simulator.jar"
touch -t 202201010000 "$P/pom.xml"
run "$P"
assert_contains "pom.xml lebih baru dari JAR: build ulang" "$LOG" "MVN:"

new_project; fresh_jar "$P"
touch -t 202101010000 "$P/target/credit-simulator.jar"
rm "$P/src/main/A.java"
run "$P"
assert_contains "file source dihapus: build ulang (JAR lama masih berisi class itu)" "$LOG" "MVN:"

echo "# CREDIT_SIMULATOR_JAR"
new_project
echo "jar" > "$WORK/custom.jar"
CREDIT_SIMULATOR_JAR="$WORK/custom.jar" run "$P"
assert_not_contains "JAR kustom: build dilewati" "$LOG" "MVN:"
assert_contains "JAR kustom dipakai" "$LOG" "ARGS: -jar $WORK/custom.jar"

CREDIT_SIMULATOR_JAR="$WORK/tidak_ada.jar" run "$P"
assert_eq "JAR kustom tidak ada: kode keluar 1" "1" "$CODE"
assert_contains "JAR kustom tidak ada: pesan jelas" "$ERR" "JAR tidak ditemukan"

echo "# pengecekan Java"
new_project; fresh_jar "$P"
FAKE_PATH="$ONLY_MVN" run "$P"
assert_eq "tanpa Java: kode keluar 1" "1" "$CODE"
assert_contains "tanpa Java: pesan jelas" "$ERR" "Java tidak ditemukan"

for old in 1.8.0_392:8 11.0.21:11 16.0.2:16; do
  FAKE_JAVA_VERSION="${old%%:*}" run "$P"
  assert_eq "Java ${old%%:*} ditolak: kode keluar 1" "1" "$CODE"
  assert_contains "Java ${old%%:*} ditolak: sebut versi ${old##*:}" "$ERR" "terdeteksi: Java ${old##*:}"
done

for good in 17 17.0.9 21 21.0.1 25-ea; do
  FAKE_JAVA_VERSION="$good" run "$P"
  assert_eq "Java $good diterima" "0" "$CODE"
done

FAKE_JAVA_VERSION="abc" run "$P"
assert_contains "versi Java tidak terbaca: pesan jelas" "$ERR" "Versi Java tidak terbaca"

echo "# pengecekan Maven dan kegagalan build"
new_project
FAKE_PATH="$ONLY_JAVA" run "$P"
assert_eq "perlu build tapi tanpa Maven: kode keluar 1" "1" "$CODE"
assert_contains "tanpa Maven: pesan jelas" "$ERR" "Maven (mvn) tidak ditemukan"

new_project; fresh_jar "$P"
FAKE_PATH="$ONLY_JAVA" run "$P"
assert_eq "JAR sudah ada: Maven tidak dibutuhkan" "0" "$CODE"

new_project
FAKE_MVN_FAIL=1 run "$P"
assert_eq "build gagal: kode keluar 1" "1" "$CODE"
assert_contains "build gagal: pesan jelas" "$ERR" "Build gagal"
assert_not_contains "build gagal: aplikasi tidak dijalankan" "$LOG" "ARGS:"

echo "# perilaku saat menjalankan"
new_project; fresh_jar "$P"
FAKE_JAVA_EXIT=2 run "$P"
assert_eq "kode keluar aplikasi (2) diteruskan" "2" "$CODE"
FAKE_JAVA_EXIT=1 run "$P"
assert_eq "kode keluar aplikasi (1) diteruskan" "1" "$CODE"

ELSEWHERE="$WORK/tempat lain"; mkdir -p "$ELSEWHERE"; ELSEWHERE="$(cd -P "$ELSEWHERE" && pwd)"
RUN_CWD="$ELSEWHERE" run "$P" input.txt
assert_contains "folder kerja tetap milik pemanggil (path file relatif aman)" "$LOG" "CWD: $ELSEWHERE"

STDIN="show" run "$P"
assert_contains "stdin diteruskan ke aplikasi" "$LOG" "STDIN: show"

echo "# symlink"
mkdir -p "$WORK/linkdir"
ln -s "$P/credit_simulator" "$WORK/linkdir/cs_root"
SCRIPT="$WORK/linkdir/cs_root" RUN_CWD="$ELSEWHERE" run "$P"
assert_contains "symlink absolut ke ./credit_simulator" "$LOG" "ARGS: -jar $P/target/credit-simulator.jar"

ln -s "$P/bin/credit_simulator" "$WORK/linkdir/cs_bin"
SCRIPT="$WORK/linkdir/cs_bin" RUN_CWD="$ELSEWHERE" run "$P"
assert_contains "symlink absolut ke bin/credit_simulator" "$LOG" "ARGS: -jar $P/target/credit-simulator.jar"
assert_not_contains "symlink: tidak salah membangun di folder lain" "$LOG" "MVN:"

ln -s "../$(basename "$P")/bin/credit_simulator" "$WORK/linkdir/cs_rel"
mkdir -p "$WORK/dalam/sekali"
SCRIPT="$WORK/linkdir/cs_rel" RUN_CWD="$WORK/dalam/sekali" run "$P"
assert_contains "symlink relatif ke bin/credit_simulator" "$LOG" "ARGS: -jar $P/target/credit-simulator.jar"

echo "# kebersihan file di repositori"
for f in credit_simulator bin/credit_simulator test/credit_simulator_test.sh .gitattributes; do
  if [[ ! -f "$ROOT/$f" ]]; then fail "$f ada"; continue; fi
  if LC_ALL=C grep -q $'\r' "$ROOT/$f"; then
    fail "$f memakai LF" "ditemukan CRLF; ubah ke LF"
  else
    ok "$f memakai LF"
  fi
done
for f in credit_simulator bin/credit_simulator test/credit_simulator_test.sh; do
  assert_eq "$f diawali shebang bash" "#!/usr/bin/env bash" "$(head -n 1 "$ROOT/$f")"
done

if command -v git >/dev/null 2>&1 && git -C "$ROOT" rev-parse --git-dir >/dev/null 2>&1; then
  for f in credit_simulator bin/credit_simulator test/credit_simulator_test.sh; do
    entry="$(git -C "$ROOT" ls-files -s -- "$f")"
    if [[ -z "$entry" ]]; then
      fail "$f tercatat di git" "belum di-git add"
    else
      assert_eq "$f tercatat executable di git (100755)" "100755" "${entry%% *}"
    fi
  done
else
  echo "  skip - cek mode executable git (bukan repositori git atau git tidak ada)"
fi

echo
echo "$PASS lulus, $FAIL gagal"
if (( FAIL > 0 )); then
  exit 1
fi
