   unit indented; // Noncompliant

  interface // Noncompliant

   uses // Noncompliant
  SysUtils,
  System;

  type // Noncompliant
    TMyObject = class(TObject)
    end;

    procedure MyProc; // Noncompliant

        implementation // Noncompliant

      uses // Noncompliant
      StrUtils,
      Math;

    type // Noncompliant
        TMyOtherObject = class(TObject)
        private
          class procedure MyClassProc;
        end;


  class procedure TMyOtherObject.MyClassProc; // Noncompliant
  begin // Noncompliant
    Writeln('Hello world');
  end; // Noncompliant

  procedure MyProc; // Noncompliant
    procedure MySubProc;
    begin
    end;
  var
    Anon: TProc;
  begin // Noncompliant
    Anon :=
    procedure begin
    end;
  end; // Noncompliant

 initialization // Noncompliant

    finalization // Noncompliant

          end. // Noncompliant