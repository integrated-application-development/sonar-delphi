unit Unit2;

{This is a sample Delphi file.}

interface

uses
    Unit1;

type
  TFooHelper2 = class helper for TFoo
    function Bar: Boolean;
  end;

implementation

function TFooHelper2.Bar: Boolean;
begin
  // Do nothing
end;

end.