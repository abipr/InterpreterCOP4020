

	@Test
	void andSimpleProgram() throws PLCException {
		String input = """
				void f(){}
				""";
		typeCheck(input);
	}

	@Test
	void andReusingTheProgName() throws PLCException {
		String input = """
				void f(){
					int f.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andRedeclaringParamNames() throws PLCException {
		String input = """
				void f(int g){
					int g.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andReusingParamNames() throws PLCException {
		String input = """
				void f(int g, string g){}
				""";
		typeCheckError(input);
	}

	@Test
	void andInstantReturn() throws PLCException {
		String input = """
				int f(int f){
					:f.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andInvalidReturn() throws PLCException {
		String input = """
				int f(string f){
					:f.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andUninitializedReturn() throws PLCException {
		String input = """
				int f(){
					int g.
					:g.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andInitializedReturn() throws PLCException {
		String input = """
				int f(){
					int g = 0.
					:g.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andInTermsOfItself() throws PLCException {
		String input = """
				int f(){
					int g = 0 + g.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andAssignInTermsOfItself() throws PLCException {
		String input = """
				int f(){
					int g.
					g = g.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andReturnCompatibility() throws PLCException {
		String input = """
				string f(){
					string g = 0.
					:g.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andMultipleDeclarations() throws PLCException {
		String input = """
				void f(){
					int g.
					int g.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andStatementThatRedeclares() throws PLCException {
		String input = """
				void f(){
					int g.
					int g = 6.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andSameNameDifferentTypes() throws PLCException {
		String input = """
				void f(){
					int g = 3.
					string g = "3".
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andAssignCompatibility() throws PLCException {
		String input = """
				void f(int i, string s, pixel p, image m){
					m = m.
					m = p.
					m = s.
					p = p.
					p = i.
					i = i.
					i = p.
					s = s.
					s = i.
					s = p.
					s = m.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andAssignIncompatibility() throws PLCException {
		String input = """
				void f(){
					int g = 3.
					g = "3".
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andTechnicallyNotAString() throws PLCException {
		String input = """
				void f(string f){
					pixel g = f[1,2].
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andTechnicallyNotAPixel() throws PLCException {
		String input = """
				void f(pixel f){
					int g = f[1,2]:grn.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andValidPixels() throws PLCException {
		String input = """
				void f(pixel f){
					int g = f:grn.
					int h = f.
					pixel i = h.
					pixel j = i.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andDeclaringImages() throws PLCException {
		String input = """
				void f(image g){
					image f = g.
					image [300,400] h.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andOneDoesNotSimplyDeclareImages() throws PLCException {
		String input = """
				void f(image g){
					image h.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andDimensionForANonImage() throws PLCException {
		String input = """
				void f(){
					int [300, 400] g.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andErrorDimension() throws PLCException {
		String input = """
				void f(){
					image [300, "400"] g.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andIfStmt() throws PLCException {
		String input = """
				void f(){
					string str = if x ? "true" ? "false".
				}
				""";
		typeCheck(input);
	}

	@Test
	void andNestedIfStmt() throws PLCException {
		String input = """
				void f(int b, int c, int d, int e){
					int f = if b == c ? (if b > d ? c ? e == d) ? (if c < e ? b <= e ? d).
				}
				""";
		typeCheck(input);
	}

	@Test
	void andIfStmtErr() throws PLCException {
		String input = """
				void f(){
					int i = if i ? "true" ? "false".
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andIfStmtUnevenTypes() throws PLCException {
		String input = """
				void f(int val){
					string str = if x ? "true" ? 3.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andImagesWithSelectors() throws PLCException {
		String input = """
				void f(int this, image anImage, int anInt, pixel aPixel){
					image [300,400] f = anImage.
					this = f:red == anImage.
					this = f[1,2] == aPixel.
					this = f[1,2]:red == anInt.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andInvalidSelectors() throws PLCException {
		String input = """
				void f(int this, image [300,400] anImage, int anInt, pixel aPixel){
					image [300,400] f = anImage.
					this = f:red == aPixel.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andAProperPixel() throws PLCException {
		String input = """
				void f(image f){
					pixel p = f[1,2].
				}
				""";
		typeCheck(input);
	}

	@Test
	void andAnImproperPixel() throws PLCException {
		String input = """
				void f(image f){
					pixel p = f[1,"2"].
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andItCannotBeVoid() throws PLCException {
		String input = """
				void f(){
					void g.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andTheseAreAllInts() throws PLCException {
		String input = """
				void f(pixel f){
					f = [1,2,3].
					f = [x,y,a].
					f = [r,Z,rand].
					f = [r,Z,rand].
					f = [x_cart[1,2], y_cart[3,4], 5].
					f = [a_polar[6,7], r_polar[8,9], 10].
				}
				""";
		typeCheck(input);
	}

	@Test
	void andTheseAreNotInts() throws PLCException {
		String input = """
				void f(pixel f){
					f = ["1", f, 3].
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andUnaryExpressions() throws PLCException {
		String input = """
				void f(int this, int i, pixel p){
					this = i == !i.
					this = p == !p.
					this = i == -i.
					this = i == cos i.
					this = i == sin i.
					this = i == atan i.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andUnaryExprTypes() throws PLCException {
		String input = """
				void f(){
					string s.
					string t = !s.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andUnaryExprTypeMismatch() throws PLCException {
		String input = """
				void f(int this, int i, pixel p){
					this = i == !p.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andBinaryExpressions() throws PLCException {
		String input = """
				void f(int this, int i, string s, pixel p, image m){
					p = if this ? p ? (p | p).
					p = if this ? p ? (p & p).

					i = if this ? i ? (i || i).
					i = if this ? i ? (i && i).

					i = if this ? i ? (i < i).
					i = if this ? i ? (i > i).
					i = if this ? i ? (i >= i).
					i = if this ? i ? (i <= i).

					i = if this ? i ? (i == i).
					i = if this ? i ? (s == s).
					i = if this ? i ? (p == p).
					i = if this ? i ? (m == m).

					i = if this ? i ? (i ** i).
					p = if this ? p ? (p ** i).

					i = if this ? i ? (i + i).
					s = if this ? s ? (s + s).
					p = if this ? p ? (p + p).
					m = if this ? m ? (m + m).

					i = if this ? i ? (i - i).
					p = if this ? p ? (p - p).
					m = if this ? m ? (m - m).

					i = if this ? i ? (i * i).
					p = if this ? p ? (p * p).
					m = if this ? m ? (m * m).
					i = if this ? i ? (i % i).
					p = if this ? p ? (p % p).
					m = if this ? m ? (m % m).
					i = if this ? i ? (i / i).
					p = if this ? p ? (p / p).
					m = if this ? m ? (m / m).

					p = if this ? p ? (p * i).
					m = if this ? m ? (m * i).
				}
				""";
		typeCheck(input);
	}

	@Test
	void andAMixOfBinaryExpressions() throws PLCException {
		String input = """
				void f(int i, string s, pixel p, image m){
					m = (m % (((p ** (p == p)) * (s == s)) == p)) - m.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andBadBinaryExpressions() throws PLCException {
		String input = """
				void f(int this, int i, string s, pixel p, image m){
					p = i ** p.
				}
				""";
		typeCheckError(input);
		input = """
				void f(int this, int i, string s, pixel p, image m){
					s = if this ? s ? (s == s).
				}
				""";
		typeCheckError(input);
		input = """
				void f(int this, int i, string s, pixel p, image m){
					s = s ** s.
				}
				""";
		typeCheckError(input);
		input = """
				void f(int this, int i, string s, pixel p, image m){
					s = s - s.
				}
				""";
		typeCheckError(input);
		input = """
				void f(int this, int i, string s, pixel p, image m){
					p = i * p.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andWriteThis() throws PLCException {
		String input = """
				void f(int this){
					write this.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andWriteUninitialized() throws PLCException {
		String input = """
				void f(){
					int this.
					write this.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andWhileStmt() throws PLCException {
		String input = """
				void f(int i){
					while i {}.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andWhileUninitialized() throws PLCException {
		String input = """
				void f(){
					int i.
					while i {}.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andNotAWhileStmt() throws PLCException {
		String input = """
				void f(string s) {
					while s {}.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andRedeclaringInWhileStmt() throws PLCException {
		String input = """
				void f(int i){
					while i {
						int i = 0.
						while i {
							int i = 1.
							while i {
								int i = 2.
								while i {}.
							}.
						}.
					}.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andRedeclaringButBad() throws PLCException {
		String input = """
				void f(int i){
					while i {
						int i.
						int i = 0.
					}.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andSameLevelScopes() throws PLCException {
		String input = """
				void f(int i){
					string s.
					while i { string s = "this". }.
					while i { string s = "that". }.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andAccessingOuterScopes() throws PLCException {
		String input = """
				void f(int i){
					string s = "string".
					while i {
						string t = s.
						while i {
							string u = t.
							while i {
								string v = s.
							}.
							u = s.
						}.
					}.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andNotAccessingInnerScopes() throws PLCException {
		String input = """
				void f(int i){
					string s.
					while i {
						string t = "inner".
					}.
					s = t.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andAccessingManyScopes() throws PLCException {
		String input = """
				void f(int this, int me, int i, string s, pixel p, image m){
					while this {
						pixel me = p.
						while this {
							string me = s.
							this = me == s.
							while this {
								int me = i.
								this = me == i.
							}.
							this = me == s.
						}.
						this = me == p.
						while this {
							image me = m.
							this = me == m.
						}.
						this = me == p.
					}.
					this = me == i.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andReturnInsideWhileStmt() throws PLCException {
		String input = """
				string f(int i) {
					string s = "yes".
					while i {
						:i.
					}.
					:s.
				}
				""";
		typeCheck(input);
	}

	@Test
	void andBadReturnInsideWhileStmt() throws PLCException {
		String input = """
				int f(int i) {
					string s = "yes".
					while i {
						:s.
					}.
					:i.
				}
				""";
		typeCheckError(input);
	}

	@Test
	void andItsSeriouslyUninitialized() throws PLCException {
		String input = """
				void f() {
					int i.
					image [i, i] m.
					pixel p = m[i, i].
					m = if i ? m ? m.
					write i.
					while i {}.
					write (i + i * i).
				}
				""";
		typeCheck(input);
	}
