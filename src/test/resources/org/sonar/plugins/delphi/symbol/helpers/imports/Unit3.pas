unit Unit3;

{This is a sample Delphi file.}

interface

uses
    Unit1;

type
  TFooHelper = class helper for TFoo
    function Bar: String;
  end;

implementation

function TFooHelper.Bar: String;
begin
  // Do nothing
end;

end.