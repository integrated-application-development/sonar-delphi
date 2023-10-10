   unit indented;

  interface

   uses
  SysUtils,
  System;

  type
    TMyObject = class(TObject)
    end;

    procedure MyProc;

        implementation

      uses
      StrUtils,
      Math;

    type
        TMyOtherObject = class(TObject)
        private
          class procedure MyClassProc;
        end;


  class procedure TMyOtherObject.MyClassProc;
  begin
    Writeln('Hello world');
  end;

  procedure MyProc;
  begin
    Writeln('Hello world');
  end;

 initialization

    finalization

          end.